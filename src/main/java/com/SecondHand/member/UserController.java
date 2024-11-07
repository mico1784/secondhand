package com.SecondHand.member;

import com.SecondHand.Purchase.Purchase;
import com.SecondHand.Purchase.PurchaseDTO;
import com.SecondHand.Purchase.PurchaseRepository;
import com.SecondHand.item.Item;
import com.SecondHand.item.ItemRepository;
import com.SecondHand.item.ItemService;
import com.SecondHand.review.Review;
import com.SecondHand.review.ReviewDTO;
import com.SecondHand.review.ReviewRepository;
import com.SecondHand.wishList.WishList;
import com.SecondHand.wishList.WishListRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.OnClose;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
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
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final WishListRepository wishListRepository;
    private final ReviewRepository reviewRepository;
    private final PurchaseRepository purchaseRepository;

    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(Model model, Principal principal, HttpSession session, @RequestParam(defaultValue = "1") Integer page) {
        boolean isLoggedIn = principal != null || session.getAttribute("username") != null;
        model.addAttribute("isLoggedIn", isLoggedIn);

        String username = principal != null ? principal.getName() : (String) session.getAttribute("username");
        model.addAttribute("username", username);

        try {
            Page<Item> itemList = itemService.getAllItems(PageRequest.of(page - 1, 6, Sort.by(Sort.Direction.DESC, "id")));
            model.addAttribute("items", itemList.getContent());
            model.addAttribute("hasPrevious", itemList.hasPrevious());
            model.addAttribute("hasNext", itemList.hasNext());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPage", itemList.getTotalPages());

            model.addAttribute("phoneItems", itemService.getItemsByCategory("휴대폰", PageRequest.of(0, 6)).getContent());
            model.addAttribute("padItems", itemService.getItemsByCategory("패드", PageRequest.of(0, 6)).getContent());
            model.addAttribute("watchItems", itemService.getItemsByCategory("워치", PageRequest.of(0, 6)).getContent());

        } catch (Exception e) {
            model.addAttribute("error", "아이템 목록을 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("items", List.of());
        }

        return "index";
    }

    @GetMapping("/home/categoryItems")
    @ResponseBody
    public List<Item> getCategoryItems(@RequestParam String category, @RequestParam Integer page, @RequestParam(defaultValue = "5") Integer size) {
        try {
            return itemService.getItemsByCategory(category, PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"))).getContent();
        } catch (Exception e) {
            return List.of();
        }
    }

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
        return "login";
    }

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

    @PostMapping("/add")
    public String createUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
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

        reviewCnt = reviewRepository.countByBoughtItem_Seller(user);
        model.addAttribute("reviewCnt", reviewCnt);
        model.addAttribute("user", user);
        model.addAttribute("formattedUserId", String.format("%08d", user.getId()));
        return "my-page";
    }

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
        List<Item> soldlist = itemRepository.findBySellerAndSituation(user, Item.ItemSituation.판매완료);
        if (soldlist == null) {
            soldlist = new ArrayList<>();  // 빈 리스트로 초기화
        }

        soldlist.forEach(item -> item.setFormattedPrice(String.format("%,d", item.getPrice())));

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

    @PostMapping("/editProfile")
    public String editProfile(@ModelAttribute User updatedUser, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        user.setName(updatedUser.getName());
        user.setPassword(userService.encodePassword(updatedUser.getPassword()));
        user.setEmail(updatedUser.getEmail());
        user.setAge(updatedUser.getAge());
        user.setPhoneNumber(updatedUser.getPhoneNumber());
        user.setGender(updatedUser.getGender());
        user.setAddress(updatedUser.getAddress());

        userRepository.save(user);

        redirectAttributes.addFlashAttribute("message", "프로필이 성공적으로 수정되었습니다!");
        return "redirect:/mypage";
    }
}
