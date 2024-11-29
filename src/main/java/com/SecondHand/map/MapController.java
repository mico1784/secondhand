//package com.SecondHand.map;
//
//import io.github.cdimascio.dotenv.Dotenv;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//
//
//@Controller
//public class MapController {
//
//    private  String kakaoMapsAppKey;
//
//    public MapController() {
//        Dotenv dotenv = Dotenv.load();
//        kakaoMapsAppKey = dotenv.get("KAKAO_MAPS_APP_KEY");
//        if (kakaoMapsAppKey == null) {
//            System.out.println("KAKAO_MAPS_APP_KEY: " + kakaoMapsAppKey);
//            throw new IllegalArgumentException("KAKAO_MAPS_APP_KEY not found in .env file");
//
//        }
//    }
//
//
//    @GetMapping("/map")
//    public String mapPage(Model model) {
//        Dotenv dotenv = Dotenv.load();
//        kakaoMapsAppKey = dotenv.get("KAKAO_MAPS_APP_KEY");
//        model.addAttribute("kakaoMapsAppKey", kakaoMapsAppKey);
//        return "map";
//    }
//}
