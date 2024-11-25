// 카테고리 필터링 함수
function filterByCategory(category) {
    // 카테고리와 가격 필터 값을 URL에 포함시켜서 페이지를 리로드
    const minPrice = document.getElementById('minPrice').value;
    const maxPrice = document.getElementById('maxPrice').value;
    let url = `/list?category=${category}`;

    // 가격 필터가 있다면 URL에 추가
    if (minPrice) {
        url += `&minPrice=${minPrice}`;
    }
    if (maxPrice) {
        url += `&maxPrice=${maxPrice}`;
    }

    // URL로 페이지 이동
    window.location.href = url;
}

// 가격 필터링 함수
function filterByPrice() {
    const minPrice = document.getElementById('minPrice').value;
    const maxPrice = document.getElementById('maxPrice').value;
    const urlParams = new URLSearchParams(window.location.search);
    const category = urlParams.get('category');  // 현재 카테고리 값 가져오기

    let url = '/list'; // 기본 URL

    // URL에 카테고리 값이 있으면 가격 필터와 함께 추가
    if (category) {
        url += `?category=${category}`;
    }

    let isFirstParam = category ? false : true; // 첫 번째 파라미터 체크

    // 가격 필터가 있으면 URL에 추가
    if (minPrice || maxPrice) {
        if (isFirstParam) {
            url += `?`;
            isFirstParam = false;
        } else {
            url += `&`;
        }
        if (minPrice) {
            url += `minPrice=${minPrice}`;
        }
        if (maxPrice) {
            url += `${minPrice ? '&' : ''}maxPrice=${maxPrice}`;
        }
    }

    // URL로 페이지 이동
    window.location.href = url;
}

// 페이지가 로드될 때 URL에서 카테고리 및 가격 필터를 가져와 필터 상태 적용
window.onload = function() {
    const urlParams = new URLSearchParams(window.location.search);

    // 카테고리 필터 상태 적용
    const category = urlParams.get('category');
    const buttons = document.querySelectorAll('.filter-button');
    buttons.forEach(button => {
        button.classList.remove('selected');
    });

    if (category === '') {
        const selectedButton = Array.from(buttons).find(button => button.textContent === '전체');
        if (selectedButton) {
            selectedButton.classList.add('selected');
        }
    } else {
        const selectedButton = Array.from(buttons).find(button => button.textContent === category);
        if (selectedButton) {
            selectedButton.classList.add('selected');
        }
    }

    // 가격 필터 상태 적용
    const minPrice = urlParams.get('minPrice');
    const maxPrice = urlParams.get('maxPrice');

    if (minPrice) {
        document.getElementById('minPrice').value = minPrice;
    }
    if (maxPrice) {
        document.getElementById('maxPrice').value = maxPrice;
    }
}
