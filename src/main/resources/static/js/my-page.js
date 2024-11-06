// 모든 패널의 ID를 배열로 정의합니다.
const panels = [
  'purchase-history-panel',
  'sales-history-panel',
  'wishlist-panel',
  'account-panel',
  'review-panel',
  'withdrawal-panel'
];

// 특정 패널을 열 때 다른 모든 패널을 닫는 함수
function togglePanel(panelId, callback) {
  panels.forEach(id => {
    const panel = document.getElementById(id);
    if (id === panelId) {
      // 지정된 패널 ID라면 'active' 클래스를 토글
      const isActive = panel.classList.contains('active');
      panel.classList.toggle('active');
      if(!isActive && typeof callback === 'function'){
        callback(); // 패널이 닫혀있을 때만 요청 보냄
      }
    } else {
      // 다른 모든 패널은 'active' 클래스를 제거하여 닫음
      panel.classList.remove('active');
    }
  });
}

// 각 패널을 여는 함수들
function togglePurchaseHistory() {
  togglePanel('purchase-history-panel');
}

function toggleSalesHistory() {
  togglePanel('sales-history-panel', loadSoldList);
}

function toggleWishlist() {
  togglePanel('wishlist-panel', loadWishList);
}

function toggleAccountPanel() {
  togglePanel('account-panel', loadUserInfo);
}

function toggleReviewPanel() {
  togglePanel('review-panel', loadReviews);
  loadReviews();
}

function toggleWithdrawalPanel() {
  togglePanel('withdrawal-panel');
}

// 거래 품목 불러오기
document.addEventListener("DOMContentLoaded", function(){   // 클릭 시 해당 목록이 보이게 해줌
    const sortLinks = document.querySelectorAll(".sort-k"); // sort-k 클래스
    const situTaps = document.querySelectorAll(".tab-k");   // tab-k 클래스

    sortLinks.forEach(link => {
        link.addEventListener("click", function(event){
            event.preventDefault();
            const sortType = this.getAttribute("data-sort");
            const activeTab = document.querySelector(".tab-k.active-k").getAttribute("data-situ");
            loadSortedItems(activeTab ,sortType);
        });
    });
    situTaps.forEach(tab => {
        tab.addEventListener("click", function(event){
            event.preventDefault();
            document.querySelector(".tab-k.active-k").classList.remove("active-k");
            this.classList.add("active-k");
            const situType = this.getAttribute("data-situ");
            loadSortedItems(situType, "latest");
        });
    });

    loadSortedItems("total","latest");
});

function loadSortedItems(situType, sortType){   // 클릭시 요청할 url을 생성해줌
    fetch(`/mypage/items?situ=${situType}&sort=${sortType}`)
        .then(response => response.json())
        .then(data => {
            const itemContainer = document.querySelector(".msl-k tbody");
            const emptyMessage = document.querySelector(".empty-message-k");
            itemContainer.innerHTML = "";   // 기존 목록을 제거하는 코드

            if(data.mySaleList.length === 0){
                emptyMessage.style.display = "block";
            }else{
                emptyMessage.style.display = "none";
                data.mySaleList.forEach(item => {
                    const itemRow = `
                        <tr>
                            <td>
                                <a href="/item/${item.id}">
                                    <img src="${item.imgURL}">
                                </a>
                            </td>
                            <td>
                                <a href="/item/${item.id}">
                                    ${item.title}
                                </a>
                            </td>
                            <td>${item.price} 원</td>
                        </tr>
                    `;
                    itemContainer.innerHTML += itemRow;
                });
            };
            const msCnt = document.querySelector(".count-k");
            msCnt.textContent = `총 ${data.msEntireCnt} 개`;
        })
        .catch(error => console.error("Error: ", error));
};

// 개인정보를 불러오기
function loadUserInfo(){
    fetch('/mypage/user-info')
        .then(response => response.json())
        .then(data => {
            const userInfoContainer = document.querySelector('.account-panel .user-info');
            userInfoContainer.innerHTML = `
                <div><strong>이름:</strong> <span>${data.user.name}</span></div>
                <div><strong>아이디:</strong> <span>${data.user.username}</span></div>
                <div><strong>이메일:</strong> <span>${data.user.email}</span></div>
                <div><strong>전화번호:</strong> <span>${data.user.phoneNumber}</span></div>
                <div><strong>성별:</strong> <span>${data.user.gender}</span></div>
                <div><strong>주소:</strong> <span>${data.user.address}</span></div>
                <p>가입일: <span>${data.formattedDate}</span></p>
                <a href="/editProfile">프로필 수정</a>
            `;
        })
        .catch(error => console.error('Error loading user info: ', error));
};

// 찜 목록을 불러오기
function loadWishList() {
    fetch('/mypage/wishlist')
        .then(response => response.json())
        .then(data => {
            wishListItems = data.wishlist; // 찜 목록을 전역 변수에 저장
            displayWishList(wishListItems); // 모든 찜 목록을 표시
        })
        .catch(error => console.error('Error loading wishList', error));
}

// 찜 목록을 화면에 표시하는 함수
function displayWishList(items) {
    const wishListContainer = document.querySelector('.wishlist-panel .wishlist');
    const noWishListMessage = document.querySelector('.no-wishlist-message');
    wishListContainer.innerHTML = ''; // 기존 목록을 초기화

    if (items.length === 0) {
        noWishListMessage.style.display = "block";
    } else {
        noWishListMessage.style.display = "none";
        items.forEach(item => {
            const itemRow = `
                <div class="wishlist-item">
                    <a href="#" onclick="checkItemBeforeRedirect(${item.itemId}, '${item.itemId}')">
                        <img src="${item.itemImgURL}" alt="상품 이미지" class="wishlist-img"/>
                    </a>
                    <a href="#" onclick="checkItemBeforeRedirect(${item.itemId}, '${item.itemId}')">
                        <strong class="wishlist-title">${item.itemTitle}</strong>
                    </a>
                    <p class="wishlist-price">${item.itemPrice} 원</p>
                    <button class="delete-button" onclick="deleteItem(${item.id})">삭제</button>
                </div>
            `;
            wishListContainer.innerHTML += itemRow;
        });
    }
}

// 상품 디테일 페이지로 이동하기 전에 확인하는 함수
function checkItemBeforeRedirect(itemId) {
    fetch(`/item/${itemId}/exists`) // 존재 여부를 확인하는 API 엔드포인트
        .then(response => {
            if (response.ok) {
                // 상품이 존재하면 디테일 페이지로 이동
                window.location.href = `/item/${itemId}`;
            } else {
                // 상품이 삭제되었으면 경고 메시지 표시
                alert("원본 게시물이 삭제되었습니다.");
            }
        })
        .catch(error => {
            console.error("Error checking item existence: ", error);
            alert("상품 정보를 확인할 수 없습니다.");
        });
}

// 검색 기능
function searchWishList() {
    const searchInput = document.querySelector('.wishlist-panel .search-input');
    const searchTerm = searchInput.value.toLowerCase(); // 소문자로 변환하여 비교
    const filteredItems = wishListItems.filter(item =>
        item.itemTitle.toLowerCase().includes(searchTerm) // 제목에 검색어가 포함되는지 확인
    );

    displayWishList(filteredItems); // 필터링된 목록을 표시
}

// DOMContentLoaded 이벤트 안에서 초기화
document.addEventListener("DOMContentLoaded", function() {
    // 검색 입력 이벤트 리스너 추가
    const searchInput = document.querySelector('.wishlist-panel .search-input');
    searchInput.addEventListener('input', searchWishList);

    // 초기 찜 목록 불러오기
    loadWishList();
});
// 찜 목록 삭제
function deleteItem(id) {
    fetch(`/wishlist/delete?id=${id}`, { method: 'DELETE' })
    .then(response => {
        if (response.ok) {
            alert('삭제완료');
            loadWishList(); // 찜 목록을 다시 로드
        } else {
            alert('삭제 실패');
        }
    })
    .catch(error => console.error('삭제 중 오류 발생', error));
}

// 판매내역 불러오기
function loadSoldList(){
    fetch('/mypage/soldlist')
        .then(response => response.json())
        .then(data => {
            const soldListContainer = document.querySelector('.sales-history-panel .soldlist');
            const emptyMessage = document.querySelector('.no-sales-history-message');
            soldListContainer.innerHTML = "";

            if(data.soldlist.length === 0) {
                emptyMessage.style.display = "block";
            }else {
                emptyMessage.style.display = "none";
                data.soldlist.forEach(item => {
                    const itemRow = `
                        <tr>
                            <td>
                                <a href="/item/${item.id}">
                                    <img src="${item.imgURL}">
                                </a>
                            </td>
                            <td>
                                <a href="/item/${item.id}">
                                    ${item.title}
                                </a>
                            </td>
                            <td>${item.price} 원</td>
                        </tr>
                    `;
                    soldListContainer.innerHTML += itemRow;
                });
            }
        })
        .catch(error => console.error('Error loading user info: ', error));
};

// 레이팅을 별로 표시하는 함수
function displayRating(rating) {
    let starsHtml = '';
    for (let i = 1; i <= 5; i++) {
        if (i <= rating) {
            starsHtml += '<span>★</span>';
        } else {
            starsHtml += '<span>☆</span>';
        }
    }
    return starsHtml;
}

// 거래 후기 불러오기
function loadReviews(){
    fetch("/mypage/reviews")
        .then(response => response.json())
        .then(data => {
            const reviewWriContainer = document.querySelector(".review-panel .revWri");
            const WriEmptyMessage = document.querySelector(".no-reviewWri-message");
            reviewWriContainer.innerHTML = "";
            if(data.reviewsWri.length === 0) {
                WriEmptyMessage.style.display = "block";
            }else {
                WriEmptyMessage.style.display = "none";
                data.reviewsWri.forEach(item => {
                    const itemRow = `
                        <div class="reviewWri">
                            <h4>${item.createdAt}</h4>
                            <div>${displayRating(item.rating)}</div>
                            <h4>${item.content}</h4>
                        </div>
                    `;
                    reviewWriContainer.innerHTML += itemRow;
                });
            }

            const reviewRecContainer = document.querySelector(".review-panel .revRec");
            const RecEmptyMessage = document.querySelector(".no-reviewRec-message");
            reviewRecContainer.innerHTML = "";
            if(data.reviewsRec.length === 0) {
                RecEmptyMessage.style.display = "block";
            }else {
                RecEmptyMessage.style.display = "none";
                data.reviewsRec.forEach(item => {
                    const itemRow = `
                        <div class="reviewRec">
                            <h4>${item.reviewerUsername}</h4>
                            <div>${displayRating(item.rating)}</div>
                            <h4>${item.createdAt}</h4>
                            <h4>${item.content}</h4>
                        </div>
                    `;
                    reviewRecContainer.innerHTML += itemRow;
                });
            }
        })
        .catch(error => console.error('Error loading reviews: ', error));
};
