 var slideIndex = 1;
    showSlides(slideIndex);

    // 다음 슬라이드로 자동 전환 (2초마다)
    setInterval(function() {
      plusSlides(1);
    }, 2000); // 2000 밀리초 = 2초


    // 다음, 이전 제어
    function plusSlides(n) {
      showSlides(slideIndex += n);
    }

    // 사진 제어
    function currentSlide(n) {
      showSlides(slideIndex = n);
    }

    function showSlides(n) {
      var i;
      var slides = document.getElementsByClassName("mySlides");
      var dots = document.getElementsByClassName("banner-dot");
      if (n > slides.length) {slideIndex = 1}
      if (n < 1) {slideIndex = slides.length}
      for (i = 0; i < slides.length; i++) {
          slides[i].style.display = "none";
      }
      for (i = 0; i < dots.length; i++) {
          dots[i].className = dots[i].className.replace(" active", "");
      }
      slides[slideIndex-1].style.display = "block";
      dots[slideIndex-1].className += " active";
    }



document.addEventListener("DOMContentLoaded", function () {
  // 모든 모바일 슬라이더에 대해 슬라이드 기능 초기화
  const mobileSliders = document.querySelectorAll(".mobile-slider");

  mobileSliders.forEach((slider) => {
    const itemGrid = slider.querySelector(".item-grid");
    const items = slider.querySelectorAll(".card");
    let currentIndex = 0;

    // 슬라이드 이동 함수
    function showSlide(index) {
      // 인덱스를 순환하도록 설정
      currentIndex = (index + items.length) % items.length;
      const translateX = -currentIndex * 100;
      itemGrid.style.transform = `translateX(${translateX}%)`;
    }

    // 다음 및 이전 슬라이드 이동 함수
    function plusSlides(n) {
      showSlide(currentIndex + n);
    }

    // 버튼 이벤트 리스너 추가
    slider.querySelector(".slide-prev").addEventListener("click", () => plusSlides(-1));
    slider.querySelector(".slide-next").addEventListener("click", () => plusSlides(1));
  });

  // 화면 크기에 따라 슬라이더 보이기 조정
  function adjustSliderVisibility() {
    const desktopSliders = document.querySelectorAll(".desktop-slider");
    const mobileSliders = document.querySelectorAll(".mobile-slider");
    const isMobile = window.innerWidth <= 768;

    desktopSliders.forEach((slider) => {
      slider.style.display = isMobile ? "none" : "flex";
    });

    mobileSliders.forEach((slider) => {
      slider.style.display = isMobile ? "block" : "none";
    });
  }

  // 페이지 로드 및 크기 조정 이벤트
  window.addEventListener("load", adjustSliderVisibility);
  window.addEventListener("resize", adjustSliderVisibility);
});


