package com.SecondHand.member;

import com.SecondHand.item.Item;
import com.SecondHand.item.ItemRepository;
import com.SecondHand.item.ItemService;
import com.SecondHand.review.Review;
import com.SecondHand.review.ReviewDTO;
import com.SecondHand.review.ReviewRepository;
import com.SecondHand.wishList.WishList;
import com.SecondHand.wishList.WishListRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@SessionAttributes("user")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final WishListRepository wishListRepository;
    private final ReviewRepository reviewRepository;

    @GetMapping("/")
    public String index(){return "redirect:/home";}

    @GetMapping("/home")
    public String home(Model model, Principal principal, HttpSession session,
                       @RequestParam(defaultValue = "1") Integer page) {

        // 로그인 여부 확인: Principal 또는 세션에 사용자 정보가 있는지 확인
        boolean isLoggedIn = principal != null || session.getAttribute("username") != null;
        model.addAttribute("isLoggedIn", isLoggedIn);

        // 사용자 이름을 설정: 일반 로그인 사용자는 Principal, 카카오 로그인 사용자는 세션에서 가져옴
        String username = null;
        if (principal != null) {
            username = principal.getName();
        } else if (session.getAttribute("username") != null) {
            username = (String) session.getAttribute("username");
        }

        model.addAttribute("username", username);

        try {
            // 전체 아이템 페이지네이션 처리
            Page<Item> itemList = itemService.getAllItems(PageRequest.of(page - 1, 5, Sort.by(Sort.Direction.DESC, "id")));
            model.addAttribute("items", itemList.getContent());
            model.addAttribute("hasPrevious", itemList.hasPrevious());
            model.addAttribute("hasNext", itemList.hasNext());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPage", itemList.getTotalPages());

            // 카테고리별 아이템 설정
            model.addAttribute("phoneItems", itemService.getItemsByCategory("휴대폰", PageRequest.of(0, 5)).getContent());
            model.addAttribute("padItems", itemService.getItemsByCategory("패드", PageRequest.of(0, 5)).getContent());
            model.addAttribute("watchItems", itemService.getItemsByCategory("워치", PageRequest.of(0, 5)).getContent());

        } catch (Exception e) {
            model.addAttribute("error", "아이템 목록을 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("items", List.of()); // 기본적으로 빈 리스트 반환
        }

        return "index";
    }

    // 카테고리별 아이템을 비동기적으로 가져오기 위한 API
    @GetMapping("/home/categoryItems")
    @ResponseBody
    public List<Item> getCategoryItems(@RequestParam String category,
                                       @RequestParam Integer page,
                                       @RequestParam(defaultValue = "5") Integer size) {
        try {
            Page<Item> items = itemService.getItemsByCategory(category, PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id")));
            return items.getContent();
        } catch (Exception e) {
            return List.of(); // 오류가 발생하면 빈 리스트 반환
        }


    }

    // 로그인 페이지를 표시하는 메소드
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
            model.addAttribute("username", user.getUsername());
        }

        if (error != null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 잘못되었습니다.");
        }
        return "login"; // login.html 파일을 반환
    }

   /* // 로그인 요청 처리
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) { // displayname을 username으로 변경
        boolean authenticated = userService.authenticateUser(username, password);

        if (authenticated) {
            return "redirect:/home"; // 로그인 성공 시 홈으로 리다이렉트
        } else {
            model.addAttribute("error", "아이디 또는 비밀번호가 잘못되었습니다.");
            return "login"; // 로그인 실패 시 로그인 페이지로 다시 리턴
        }
    }*/

    // 사용자 등록 페이지를 위한 GET 메서드
    @GetMapping("/register")
    public String showRegistrationForm(Principal principal, Model model) {
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
            model.addAttribute("username", user.getUsername());
        }
        return "register"; // register.html 파일을 반환
    }

    @GetMapping("/mypage")
    public String mypage(Model model, Principal principal, HttpSession session) {
        String username;
        User user;
        long reviewCnt;

        if (principal != null) {
            // 일반 로그인 사용자
            username = principal.getName();
            user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        } else if (Boolean.TRUE.equals(session.getAttribute("isLoggedIn"))) {
            // 카카오 로그인 사용자 확인
            username = (String) session.getAttribute("username");
            if (username == null) {
                return "redirect:/login"; // 세션에 username 없으면 로그인 페이지로 리다이렉트
            }
            user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        } else {
            return "redirect:/login"; // 인증되지 않은 경우 로그인 페이지로 리다이렉트
        }

        reviewCnt = reviewRepository.countByBoughtItem_Seller(user);

        model.addAttribute("reviewCnt", reviewCnt);
        model.addAttribute("user", user);
        model.addAttribute("formattedUserId", String.format("%08d", user.getId()));

        return "my-page";
    }

    @GetMapping("/mypage/items")
    @ResponseBody
    public Map<String, Object> getSortedItems(@RequestParam(value = "situ", defaultValue = "total")String situ,
                                              @RequestParam(value = "sort", defaultValue = "latest")String sort,
                                              Principal principal){
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        List<Item> mySaleList;

        switch (situ){
            case "onSale":
                mySaleList = itemRepository.findBySellerAndSituation(user, Item.ItemSituation.onSale);
                break;
            case "soldOut":
                mySaleList = itemRepository.findBySellerAndSituation(user, Item.ItemSituation.soldOut);
                break;
            default:
                mySaleList = itemRepository.findBySeller(user);
        }

        switch (sort){
            case "priceLow":
                mySaleList.sort(Comparator.comparing(Item::getPrice));
                break;
            case "priceHigh":
                mySaleList.sort(Comparator.comparing(Item::getPrice).reversed());
                break;
            default:
                mySaleList.sort(Comparator.comparing(Item::getUploadDate).reversed());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("mySaleList", mySaleList);
        response.put("msEntireCnt", mySaleList.size());
        return response;

    }

    @GetMapping("/mypage/user-info")
    @ResponseBody
    public Map<String, Object> getUserProfile(Principal principal){
        if(principal == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        String userName = principal.getName();
        User user =  userRepository.findByUsername(userName).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 포맷된 날짜 추가
        LocalDateTime createdAt = user.getCreatedAt();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = createdAt.format(formatter);

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("formattedDate", formattedDate);

        return response;
    }

    @GetMapping("/mypage/wishlist")
    @ResponseBody
    public Map<String, Object> getWishList(Principal principal){
        String userName = principal.getName();
        User user =  userRepository.findByUsername(userName).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        List<WishList> wishList = wishListRepository.findByUserOrderByCreatedDateDesc(user);

        Map<String, Object> response = new HashMap<>();
        response.put("wishlist", wishList);

        return response;
    }

    @GetMapping("/mypage/soldlist")
    @ResponseBody
    public Map<String, Object> getSoldList(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("사용자가 로그인되어 있지 않습니다.");
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // "soldOut" 대신 ItemSituation.SOLD_OUT 사용
        List<Item> soldlist = itemRepository.findBySellerAndSituation(user, Item.ItemSituation.soldOut);
        if (soldlist == null) {
            soldlist = new ArrayList<>();  // 빈 리스트로 초기화
        }

        Map<String, Object> response = new HashMap<>();
        response.put("soldlist", soldlist);

        return response;
    }

    @GetMapping("/mypage/reviews")
    @ResponseBody
    public Map<String, Object> getReviews(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("사용자가 로그인되어 있지 않습니다.");
        }
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        List<Review> reviewsWri = reviewRepository.findByReviewer(user);
        List<Review> reviewsRec = reviewRepository.findByBoughtItem_Seller(user);

        // Review 엔티티 리스트를 ReviewDTO 리스트로 변환
        List<ReviewDTO> reviewsWriDTO = reviewsWri.stream()
                .map(ReviewDTO::new)
                .collect(Collectors.toList());
        List<ReviewDTO> reviewsRecDTO = reviewsRec.stream()
                .map(ReviewDTO::new)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("reviewsWri", reviewsWriDTO);
        response.put("reviewsRec", reviewsRecDTO);

        return response;
    }

    // 사용자 저장
    @PostMapping("/add")
    public String createUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            userService.saveUser(
                    user.getName(),
                    user.getUsername(), // displayname을 username으로 변경
                    user.getEmail(),
                    user.getPassword(), // 해싱은 UserService에서 처리됩니다.
                    user.getPhoneNumber(),
                    user.getAddress(),
                    user.getGender(),
                    user.getAge()
            );

            // 성공 메시지 설정
            redirectAttributes.addFlashAttribute("message", "회원가입이 성공적으로 완료되었습니다!");

            return "redirect:/login"; // 가입 후 로그인 페이지로 리다이렉트

        } catch (IllegalArgumentException e) {
            // 예외 메시지를 에러 메시지로 설정
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }

    }

    @GetMapping("/editProfile")
    public String showEditProfileForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login"; // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        model.addAttribute("user", user); // 사용자 정보를 모델에 추가하여 폼의 기본값으로 사용
        return "editProfile"; // editProfile.html 파일을 반환
    }

    // 프로필 수정 처리 POST 요청
    @PostMapping("/editProfile")
    public String editProfile(@ModelAttribute User updatedUser, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 사용자 정보 업데이트
        user.setName(updatedUser.getName());
        user.setPassword(userService.encodePassword(updatedUser.getPassword())); // 암호화된 비밀번호로 설정
        user.setEmail(updatedUser.getEmail());
        user.setAge(updatedUser.getAge());
        user.setPhoneNumber(updatedUser.getPhoneNumber());
        user.setGender(updatedUser.getGender());
        user.setAddress(updatedUser.getAddress());

        userRepository.save(user); // 업데이트된 사용자 정보 저장

        redirectAttributes.addFlashAttribute("message", "프로필이 성공적으로 수정되었습니다!");
        return "redirect:/mypage"; // 수정 완료 후 마이페이지로 리다이렉트
    }
}

