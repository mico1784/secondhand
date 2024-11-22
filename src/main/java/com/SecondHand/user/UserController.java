package com.SecondHand.user;

import com.SecondHand.Purchase.PurchaseDTO;
import com.SecondHand.Purchase.PurchaseRepository;
import com.SecondHand.item.Item;
import com.SecondHand.item.ItemRepository;
import com.SecondHand.item.ItemService;
import com.SecondHand.item.S3Service;
import com.SecondHand.review.ReviewDTO;
import com.SecondHand.review.ReviewRepository;
import com.SecondHand.wishList.WishList;
import com.SecondHand.wishList.WishListRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor // 필요한 의존성을 자동으로 주입
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final WishListRepository wishListRepository;
    private final ReviewRepository reviewRepository;
    private final PurchaseRepository purchaseRepository;
    private final S3Service s3Service;

    // 홈 페이지로 리다이렉트
    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }

    // 홈 페이지 렌더링
    @GetMapping("/home")
    public String home(Model model, Principal principal, HttpSession session, @RequestParam(defaultValue = "1") Integer page) {
        boolean isLoggedIn = principal != null || session.getAttribute("username") != null;
        model.addAttribute("isLoggedIn", isLoggedIn);

        String username = principal != null ? principal.getName() : (String) session.getAttribute("username");
        model.addAttribute("username", username);

        if (principal != null) {
            // 로그인된 사용자 정보 모델에 추가
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
            model.addAttribute("user", user);
        }

        try {
            // 페이지네이션 처리된 아이템 목록 가져오기
            Page<Item> itemList = itemService.getAllItems(PageRequest.of(page - 1, 6, Sort.by(Sort.Direction.DESC, "id")));
            List<Item> items = itemList.getContent();

            // 아이템의 가격을 포맷팅하여 모델에 추가
            items.forEach(item -> item.setFormattedPrice(String.format("%,d", item.getPrice())));

            model.addAttribute("items", items);
            model.addAttribute("hasPrevious", itemList.hasPrevious());
            model.addAttribute("hasNext", itemList.hasNext());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPage", itemList.getTotalPages());

            // 카테고리별 아이템 리스트 추가 - 모든 아이템 가져오기
            List<Item> phoneItems = itemService.getAllItemsByCategorySorted("휴대폰", Sort.by(Sort.Direction.DESC, "uploadDate"));
            model.addAttribute("phoneItems", phoneItems);

            List<Item> padItems = itemService.getAllItemsByCategorySorted("패드", Sort.by(Sort.Direction.DESC, "uploadDate"));
            model.addAttribute("padItems", padItems);

            List<Item> watchItems = itemService.getAllItemsByCategorySorted("워치", Sort.by(Sort.Direction.DESC, "uploadDate"));
            model.addAttribute("watchItems", watchItems);

            List<Item> recentItems = itemService.getRecentItems(); // 모든 최신 게시물
            model.addAttribute("recentItems", recentItems);

        } catch (Exception e) {
            model.addAttribute("error", "아이템 목록을 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("items", List.of());
        }

        return "index";
    }


    // 특정 카테고리의 아이템을 가져오는 메서드
    @GetMapping("/home/categoryItems")
    @ResponseBody
    public List<Item> getCategoryItems(
            @RequestParam String category,
            @RequestParam Integer page,
            @RequestParam(defaultValue = "5") Integer size,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(defaultValue = "id") String sortBy) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy); // 정렬 기준 설정
            Pageable pageable = PageRequest.of(page - 1, size, sort); // 페이지 요청 생성
            return itemService.getAllItemsByCategoryPaged(category, pageable).getContent(); // 페이지 요청 전달
        } catch (Exception e) {
            return List.of(); // 예외 시 빈 리스트 반환
        }
    }


    // 로그인 페이지 렌더링
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model, Principal principal, HttpServletRequest request) {
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
            model.addAttribute("username", user.getUsername());
        }

        if (error != null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 잘못되었습니다.");
        }

        // 로그인 페이지로 넘어오기 전에 요청했던 URL을 세션에 저장
        String referer = request.getHeader("Referer");
        if (referer != null) {
            request.getSession().setAttribute("redirectUrl", referer);
        }

        return "login";
    }

    // 회원가입 페이지 렌더링
    @GetMapping("/register")
    public String showRegistrationForm(Principal principal, Model model) {
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
            model.addAttribute("username", user.getUsername());
        }
        return "register";
    }

    // 사용자 등록 처리
    @PostMapping("/add")
    public String createUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            // 비밀번호 및 사용자 정보 유효성 검사
            if (user.getPassword().length() < 8) {
                throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
            }
            if (!user.getPassword().matches(".*[A-Z].*")) {
                throw new IllegalArgumentException("비밀번호에는 최소 하나의 대문자가 포함되어야 합니다.");
            }
            if (!user.getPassword().matches(".*[a-z].*")) {
                throw new IllegalArgumentException("비밀번호에는 최소 하나의 소문자가 포함되어야 합니다.");
            }
            if (!user.getPassword().matches(".*[0-9].*")) {
                throw new IllegalArgumentException("비밀번호에는 최소 하나의 숫자가 포함되어야 합니다.");
            }
            if (user.getAge() < 19000101 || user.getAge() > 20231231) {
                throw new IllegalArgumentException("올바른 생년월일 형식을 입력해 주세요 (예: 19950430).");
            }
            if (!user.getEmail().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                throw new IllegalArgumentException("올바른 이메일 형식을 입력해 주세요.");
            }
            if (!user.getPhoneNumber().matches("^010\\d{7,8}$")) {
                throw new IllegalArgumentException("전화번호는 010으로 시작하는 10자리 또는 11자리 숫자 형식이어야 합니다. 예: 01012345678");
            }

            userService.saveUser(
                    user.getName(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getPhoneNumber(),
                    user.getAddress(),
                    user.getGender(),
                    user.getAge()
            );
            redirectAttributes.addFlashAttribute("message", "회원가입이 성공적으로 완료되었습니다!");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    // 마이페이지 렌더링
    @GetMapping("/mypage")
    public String mypage(Model model, Principal principal, HttpSession session) {
        String username;
        User user;
        long reviewCnt;

        if (principal != null) {
            username = principal.getName();
            user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
            session.setAttribute("username", username);
            session.setAttribute("isLoggedIn", true);
        } else if (session.getAttribute("isLoggedIn") != null && (boolean) session.getAttribute("isLoggedIn")) {
            username = (String) session.getAttribute("username");
            if (username == null) {
                return "redirect:/login";
            }
            user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        } else {
            return "redirect:/login";
        }

        // 리뷰 개수 가져오기
        reviewCnt = reviewRepository.countByBoughtItem_Seller(user);
        model.addAttribute("reviewCnt", reviewCnt);
        model.addAttribute("user", user);
        model.addAttribute("formattedUserId", String.format("%08d", user.getId()));
        return "my-page";
    }

    // 판매 중인 아이템 조회
    @GetMapping("/mypage/items")
    @ResponseBody
    public Map<String, Object> getSortedItems(@RequestParam(value = "situ", defaultValue = "total") String situ,
                                              @RequestParam(value = "sort", defaultValue = "latest") String sort,
                                              Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        List<Item> mySaleList = switch (situ) {
            case "onSale" -> itemRepository.findBySellerAndSituation(user, Item.ItemSituation.판매중);
            case "soldOut" -> itemRepository.findBySellerAndSituation(user, Item.ItemSituation.판매완료);
            default -> itemRepository.findBySeller(user);
        };

        // 아이템 정렬
        mySaleList.forEach(item -> item.setFormattedPrice(String.format("%,d", item.getPrice())));
        mySaleList = switch (sort) {
            case "priceLow" -> mySaleList.stream().sorted(Comparator.comparing(Item::getPrice)).collect(Collectors.toList());
            case "priceHigh" -> mySaleList.stream().sorted(Comparator.comparing(Item::getPrice).reversed()).collect(Collectors.toList());
            default -> mySaleList.stream().sorted(Comparator.comparing(Item::getUploadDate).reversed()).collect(Collectors.toList());
        };

        Map<String, Object> response = new HashMap<>();
        response.put("mySaleList", mySaleList);
        response.put("msEntireCnt", mySaleList.size());
        return response;
    }

    // 사용자 프로필 정보 조회
    @GetMapping("/mypage/user-info")
    @ResponseBody
    public Map<String, Object> getUserProfile(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        String userName = principal.getName();
        User user = userRepository.findByUsername(userName).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        LocalDateTime createdAt = user.getCreatedAt();
        String formattedDate = createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("formattedDate", formattedDate);
        return response;
    }

    // 찜한 목록 조회
    @GetMapping("/mypage/wishlist")
    @ResponseBody
    public Map<String, Object> getWishList(Principal principal) {
        String userName = principal.getName();
        User user = userRepository.findByUsername(userName).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        List<WishList> wishList = wishListRepository.findByUserOrderByCreatedDateDesc(user);
        wishList.forEach(item -> item.setFormattedPrice(String.format("%,d", item.getItemPrice())));

        Map<String, Object> response = new HashMap<>();
        response.put("wishlist", wishList);
        return response;
    }

    // 판매 완료된 목록 조회
    @GetMapping("/mypage/soldlist")
    @ResponseBody
    public Map<String, Object> getSoldList(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("사용자가 로그인되어 있지 않습니다.");
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        List<Item> soldlist = itemRepository.findBySellerAndSituation(user, Item.ItemSituation.판매완료);
        if (soldlist == null) {
            soldlist = new ArrayList<>();
        }

        soldlist.forEach(item -> item.setFormattedPrice(String.format("%,d", item.getPrice())));

        Map<String, Object> response = new HashMap<>();
        response.put("soldlist", soldlist);
        return response;
    }

    // 사용자 작성 및 받은 리뷰 조회
    @GetMapping("/mypage/reviews")
    @ResponseBody
    public Map<String, Object> getReviews(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("사용자가 로그인되어 있지 않습니다.");
        }
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        List<ReviewDTO> reviewsWriDTO = reviewRepository.findByReviewerOrderByCreatedAtDesc(user).stream()
                .map(ReviewDTO::new)
                .collect(Collectors.toList());
        List<ReviewDTO> reviewsRecDTO = reviewRepository.findByBoughtItem_SellerOrderByCreatedAtDesc(user).stream()
                .map(ReviewDTO::new)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("reviewsWri", reviewsWriDTO);
        response.put("reviewsRec", reviewsRecDTO);
        return response;
    }

    // 구매 내역 조회
    @GetMapping("/mypage/cart")
    @ResponseBody
    public Map<String, Object> getCart(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("사용자가 로그인되어 있지 않습니다.");
        }
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        List<PurchaseDTO> purchaseListDTO = purchaseRepository.findByBuyerOrderByPurchasedDateDesc(user).stream()
                .map(PurchaseDTO::new)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("purchaseList", purchaseListDTO);
        return response;
    }

    // 프로필 수정 폼 페이지
    @GetMapping("/editProfile")
    public String showEditProfileForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        model.addAttribute("user", user);
        return "editProfile";
    }

    // 프로필 수정 처리
    @PostMapping("/editProfile")
    public String editProfile(@ModelAttribute User updatedUser,
                              @RequestParam(value = "profileImage", required = false) MultipartFile file,
                              Principal principal,
                              RedirectAttributes redirectAttributes) throws IOException {
        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 사용자 정보 업데이트
        user.setName(updatedUser.getName());

        // 비밀번호 필드가 비어 있으면 기존 비밀번호 유지
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            user.setPassword(userService.encodePassword(updatedUser.getPassword()));
        }

        user.setEmail(updatedUser.getEmail());
        user.setAge(updatedUser.getAge());
        user.setPhoneNumber(updatedUser.getPhoneNumber());
        user.setGender(updatedUser.getGender());
        user.setAddress(updatedUser.getAddress());

        // 프로필 이미지 파일 업로드 처리
        if (file != null && !file.isEmpty()) {
            try {
                String profileImageURL = s3Service.uploadFile(file); // 파일 업로드 후 URL 반환
                user.setProfileImageURL(profileImageURL); // 사용자 객체에 이미지 URL 설정
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("error", "프로필 이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
                return "redirect:/editProfile"; // 업로드 실패 시 프로필 수정 페이지로 리다이렉트
            }
        }

        userRepository.save(user);

        redirectAttributes.addFlashAttribute("message", "프로필이 성공적으로 수정되었습니다!");
        return "redirect:/mypage"; // 프로필 페이지로 리다이렉트
    }


    // 아이디 중복 검사 API
    @GetMapping("/check-username")
    @ResponseBody
    public Map<String, Boolean> checkUsername(@RequestParam String username) {
        boolean isAvailable = !userRepository.existsByUsername(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);
        return response;
    }

    // 회원 탈퇴 처리
    @PostMapping("/deleteAccount")
    public String deleteAccount(Principal principal, HttpSession session, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인 후에 회원탈퇴를 진행할 수 있습니다.");
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        try {
            // 사용자 관련 데이터 삭제
            reviewRepository.deleteByReviewer(user);
            purchaseRepository.deleteByBuyer(user);
            wishListRepository.deleteByUser(user);
            itemRepository.deleteBySeller(user);

            // 사용자 삭제
            userRepository.delete(user);

            // 세션 종료
            session.invalidate();

            redirectAttributes.addFlashAttribute("message", "회원탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "회원탈퇴 처리 중 오류가 발생했습니다.");
        }

        return "redirect:/login"; // 탈퇴 후 로그인 페이지로 리다이렉트
    }
}
