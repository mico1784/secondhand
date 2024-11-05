package com.SecondHand.member;

import com.SecondHand.item.Item;
import com.SecondHand.item.ItemRepository;
import com.SecondHand.item.ItemService;
import com.SecondHand.wishList.WishList;
import com.SecondHand.wishList.WishListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final WishListRepository wishListRepository;

    @GetMapping("/")
    public String index(){return "redirect:/home";}

    @GetMapping("/home")
    public String home(Model model, Principal principal,
                       @RequestParam(defaultValue = "1") Integer page) {
        boolean isLoggedIn = principal != null;
        model.addAttribute("isLoggedIn", isLoggedIn);

        if (isLoggedIn) {
            model.addAttribute("username", principal.getName());
        }

        try {
            // 전체 아이템 페이지네이션
            Page<Item> itemList = itemService.getAllItems(PageRequest.of(page - 1, 5, Sort.by(Sort.Direction.DESC, "id")));
            model.addAttribute("items", itemList.getContent());
            model.addAttribute("hasPrevious", itemList.hasPrevious());
            model.addAttribute("hasNext", itemList.hasNext());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPage", itemList.getTotalPages());

            // 첫 페이지의 카테고리별 아이템 설정
            model.addAttribute("phoneItems", itemService.getItemsByCategory("휴대폰", PageRequest.of(0, 5)).getContent());
            model.addAttribute("padItems", itemService.getItemsByCategory("패드", PageRequest.of(0, 5)).getContent());
            model.addAttribute("watchItems", itemService.getItemsByCategory("워치", PageRequest.of(0, 5)).getContent());

        } catch (Exception e) {
            model.addAttribute("error", "아이템 목록을 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            // 기본적으로 빈 리스트를 반환할 수 있습니다.
            model.addAttribute("items", List.of());
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
    public String mypage(Model model, Principal principal){
        if (principal == null) {
            return "redirect:/login"; // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        }

            String username = principal.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

            model.addAttribute("user", user); // 사용자 정보를 모델에 추가

        // 포맷 처리된 유저ID
        model.addAttribute("formattedUserId", String.format("%08d", user.getId()));
        model.addAttribute("user", user); // 사용자 정보를 모델에 추가

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
                mySaleList = itemRepository.findBySellerAndSituation(user, "onSale");
                break;
            case "soldOut":
                mySaleList = itemRepository.findBySellerAndSituation(user, "soldOut");
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
    public Map<String, Object> getSoldList(Principal principal){
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        List<Item> soldlist = itemRepository.findBySellerAndSituation(user, "soldOut");

        Map<String, Object> response = new HashMap<>();
        response.put("soldlist", soldlist);

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
    @GetMapping("/withdraw")
    public String withdraw(){
        return "withdraw";
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

