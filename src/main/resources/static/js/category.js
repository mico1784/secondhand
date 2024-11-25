function updateSubcategories() {
    const category = document.getElementById("category").value;
    const subcategorySelect = document.getElementById("subcategory");
    subcategorySelect.innerHTML = "";  // 기존 옵션 비우기

    let subcategories = [];

    if (category === "휴대폰") {
        subcategories = ["삼성", "애플"];
    } else if (category === "패드") {
        subcategories = ["삼성", "애플"];
    } else if (category === "워치") {
        subcategories = ["삼성", "애플"];
    }

    subcategories.forEach(function(subcategory) {
        const option = document.createElement("option");
        option.value = subcategory;
        option.text = subcategory;
        subcategorySelect.add(option);
    });

    // 서브카테고리 초기화
    const selectedSubcategory = document.getElementById("subcategory").getAttribute("data-selected");
    if (selectedSubcategory) {
        subcategorySelect.value = selectedSubcategory;
    }
}

// 페이지가 로드될 때, 이미 선택된 카테고리에 맞는 서브카테고리 설정
window.onload = function() {
    const category = document.getElementById("category").value;

    // 카테고리가 이미 선택된 상태라면 서브카테고리 업데이트
    if (category) {
        updateSubcategories();
    }
};


// 지하철역 목록 (배열로 저장)
const subwayStations = [
  "서면", "해운대", "광안리", "사상", "부산역", "자갈치",
  "경성대", "명지", "동래", "연산", "덕천", "송정", "아미",
  "기장", "수영"
];

// location select 요소
const locationSelect = document.getElementById("location");

// 부산 지하철역을 select 옵션에 추가
subwayStations.forEach(function(station) {
  const option = document.createElement("option");
  option.value = station;
  option.textContent = station;
  locationSelect.appendChild(option);
});