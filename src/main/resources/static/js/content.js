let currentPage = {
    'phoneSlider': 1,
    'padSlider': 1,
    'watchSlider': 1
};

function moveSlide(sliderId, direction) {
    const categoryMap = {
        'phoneSlider': '휴대폰',
        'padSlider': '패드',
        'watchSlider': '워치'
    };
    const category = categoryMap[sliderId];
    const itemsPerPage = 5; // 항상 5개씩 표시

    // 페이지 업데이트
    currentPage[sliderId] += direction;
    if (currentPage[sliderId] < 1) {
        currentPage[sliderId] = 1; // 첫 페이지에서 더 뒤로 가지 않도록 설정
    }

    fetch(`/home/categoryItems?category=${category}&page=${currentPage[sliderId]}&size=${itemsPerPage}`)
        .then(response => response.json())
        .then(items => {
            const sliderContainer = document.getElementById(sliderId);
            sliderContainer.innerHTML = ''; // 기존 아이템 제거

            items.forEach(item => {
                const itemElement = document.createElement('div');
                itemElement.classList.add('card');
                itemElement.style.flex = '1 1 calc(20% - 20px)';
                itemElement.style.boxSizing = 'border-box';

                itemElement.innerHTML = `
                    <a href="/item/${item.id}">
                        <img src="${item.imgURL}" alt="상품 이미지" style="width: 100%;">
                        <div class="text-box">
                            <h5>${item.title}</h5>
                            <span>${item.price}</span>
                        </div>
                    </a>
                `;
                sliderContainer.appendChild(itemElement);
            });
        })
        .catch(error => console.error('아이템을 불러오는 중 오류가 발생했습니다:', error));
}

// 페이지 로드 시 모든 슬라이더의 첫 페이지를 로드
document.addEventListener("DOMContentLoaded", function() {
    moveSlide('phoneSlider', 0);
    moveSlide('padSlider', 0);
    moveSlide('watchSlider', 0);
});
