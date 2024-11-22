document.addEventListener("DOMContentLoaded", function () {
    // 슬라이더 ID 목록
    ['phoneSlider', 'padSlider', 'watchSlider', 'recentSlider'].forEach(sliderId => {
        const sliderContainer = document.getElementById(sliderId);
        if (!sliderContainer) {
            console.error(`슬라이더 컨테이너를 찾을 수 없습니다: ${sliderId}`);
            return;
        }

        let sliderTrack = sliderContainer.querySelector('.item-slider');
        if (!sliderTrack) {
            sliderTrack = document.createElement('div');
            sliderTrack.classList.add('item-slider');
            sliderTrack.style.display = 'flex';
            sliderTrack.style.transition = 'transform 0.4s ease-in-out';
            sliderTrack.style.transform = 'translateX(0px)'; // 초기 위치 설정

            // 기존의 카드 요소를 슬라이더 트랙으로 이동
            const cardElements = sliderContainer.querySelectorAll('.card');
            if (cardElements.length === 0) {
                console.warn(`카드 요소가 없습니다: ${sliderId}`);
            } else {
                // 모든 섹션에서 순서를 유지 (reverse 제거)
                const elementsToAdd = Array.from(cardElements); // 그대로 추가

                elementsToAdd.forEach(card => {
                    // 제목 글자수 제한 적용
                    const titleElement = card.querySelector('h5');
                    if (titleElement && titleElement.textContent.length > 10) {
                        titleElement.textContent = titleElement.textContent.substring(0, 10) + '...';
                    }
                    sliderTrack.append(card); // 순서를 유지하며 추가
                });

                sliderContainer.appendChild(sliderTrack);
                console.log(`슬라이더 트랙이 추가되었습니다: ${sliderId}`);
            }
        }

        sliderTrack.dataset.translateX = '0'; // 초기 위치 설정
    });
});


function moveSlide(sliderId, direction) {
    const sliderContainer = document.getElementById(sliderId);
    if (!sliderContainer) {
        console.error(`슬라이더 컨테이너를 찾을 수 없습니다: ${sliderId}`);
        return;
    }

    const sliderTrack = sliderContainer.querySelector('.item-slider');
    if (!sliderTrack) {
        console.error(`슬라이더 트랙을 찾을 수 없습니다: ${sliderId}`);
        return;
    }

    const cardElements = sliderTrack.querySelectorAll('.card');
    if (cardElements.length === 0) {
        console.error('슬라이드 아이템이 없습니다.');
        return;
    }

    const itemWidth = cardElements[0].offsetWidth; // CSS에서 정의된 카드 너비를 사용
    const gap = parseFloat(getComputedStyle(sliderTrack).gap) || 0; // CSS에서 간격 가져오기
    const moveDistance = itemWidth + gap; // 카드 너비와 간격을 합쳐 이동 거리 계산

    let currentTranslateX = parseInt(sliderTrack.dataset.translateX || '0', 10);
    const totalItems = cardElements.length;
    const visibleItems = 6; // 한 번에 보이는 슬라이드 아이템 수

    // maxTranslateX 계산: 마지막 카드 이후로 이동하지 않도록 범위 제한
    const maxTranslateX = -((totalItems - visibleItems) * moveDistance); // visibleItems 만큼만 보여주기 위해 조정

    // 이동 방향에 맞게 translateX 값을 변경
    currentTranslateX -= direction * moveDistance; // 한 번에 한 카드 너비만큼 이동

    // 현재 translateX 값이 범위를 벗어나지 않도록 제한
    if (currentTranslateX > 0) {
        currentTranslateX = 0; // 첫 번째 아이템으로 돌아가지 않도록 설정
    } else if (currentTranslateX < maxTranslateX) {
        currentTranslateX = maxTranslateX; // 마지막 아이템 이후로 넘어가지 않도록 설정
    }

    // 슬라이드 이동
    sliderTrack.style.transform = `translateX(${currentTranslateX}px)`;
    sliderTrack.dataset.translateX = currentTranslateX; // 현재 위치 저장
}
