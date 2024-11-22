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
  togglePanel('purchase-history-panel', loadPurchaseList);
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
                            <td>${item.formattedPrice} 원</td>
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
          // 스타일을 동적으로 삽입
                    const style = document.createElement('style');
                    style.innerHTML = `
                        /* 테이블 스타일 */
                   /* 테이블 스타일 */
                   .record-table {
                       width: 100%;
                       margin-top: 20px;
                       border-collapse: collapse;
                       background-color: #ffffff;
                       box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
                       border-radius: 10px;
                       overflow: hidden;
                   }

                   /* 테이블 헤더 */
                   .record-table th {
                       padding: 14px 18px;
                       text-align: left;
                       font-size: 16px;
                       color: #ffffff;
                       background: linear-gradient(135deg, #3498db, #2ecc71); /* 그라데이션 효과 */
                       font-weight: 600;
                       letter-spacing: 1px;
                       border-bottom: 3px solid #f1f1f1;
                   }

                   /* 테이블 데이터 셀 */
                   .record-table th,
                   .record-table td {
                       padding: 14px 18px;
                       text-align: left;
                       font-size: 15px;
                       color: #333;
                       border-bottom: 2px solid #f4f4f4;
                   }

                   /* 각 열에 대해 너비 설정 */
                   .record-table th:nth-child(1), .record-table td:nth-child(1) {
                       width: 30%; /* 첫 번째 열(왼쪽 열)의 너비를 30%로 설정 */
                   }

                   .record-table th:nth-child(2), .record-table td:nth-child(2) {
                       width: 70%; /* 두 번째 열(오른쪽 열)의 너비를 70%로 설정 */
                   }

                   /* 테이블 데이터 셀 (마우스 오버 시 효과) */
                   .record-table tr:hover {
                       background-color: #f8f8f8;
                       transform: translateY(-2px);
                       box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
                       cursor: pointer;
                   }

                   /* 마지막 행의 border-bottom 제거 */
                   .record-table tr:last-child td {
                       border-bottom: none;
                   }

                   /* 프로필 아이콘 스타일 */
                   .profile-icon {
                       font-size: 70px;
                       color: #3498db;
                       margin-bottom: 20px;
                       padding: 10px;
                       border-radius: 50%;
                       box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
                   }

                   /* 사용자 프로필 스타일 */
                   .user-profile {
                       display: flex;
                       flex-direction: column;
                       align-items: center;
                       font-family: 'Roboto', sans-serif;
                       color: #333;
                       padding: 30px;
                       background-color: #f9f9f9;
                       border-radius: 10px;
                       box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
                   }

                   /* 프로필 수정 버튼 */
                   .edit-profile-link {
                       display: inline-block;
                       margin-top: 25px;
                       padding: 12px 20px;
                       background-color: #3498db;
                       color: #fff;
                       text-decoration: none;
                       font-size: 18px;
                       border-radius: 5px;
                       box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
                       transition: background-color 0.3s ease, transform 0.3s ease;
                   }

                   /* 버튼 호버 효과 */
                   .edit-profile-link:hover {
                       background-color: #2980b9;
                       transform: translateY(-4px);
                   }

                   /* 버튼 클릭 시 효과 */
                   .edit-profile-link:active {
                       background-color: #1c5980;
                   }

                   /* 반응형 디자인 */
                   @media screen and (max-width: 768px) {
                       .record-table th, .record-table td {
                           font-size: 13px;
                           padding: 10px 12px;
                       }

                       .profile-icon {
                           font-size: 50px;
                       }

                       .edit-profile-link {
                           font-size: 16px;
                           padding: 10px 16px;
                       }
                   }

                   @media screen and (max-width: 480px) {
                       .record-table th, .record-table td {
                           font-size: 12px;
                           padding: 8px 10px;
                       }

                       .profile-icon {
                           font-size: 45px;
                       }

                       .edit-profile-link {
                           font-size: 14px;
                           padding: 8px 12px;
                       }


                      }

                        }
                    `;
                    document.head.appendChild(style); // 스타일을 <head>에 추가

           const userInfoContainer = document.querySelector('.account-panel .user-info');
           userInfoContainer.innerHTML = `
               <div class="user-profile">
                   ${
                       data.user.profileImageURL
                       ? `<img src="${data.user.profileImageURL}" alt="프로필 사진" class="profile-icon">`
                       : `<i class="fa-regular fa-user profile-icon"></i>`
                   }
                   <table class="record-table">
                       <tbody>
                           <tr><td><strong>닉네임:</strong></td><td>${data.user.name}</td></tr>
                           <tr><td><strong>아이디:</strong></td><td>${data.user.username}</td></tr>
                           <tr><td><strong>이메일:</strong></td><td>${data.user.email}</td></tr>
                           <tr><td><strong>전화번호:</strong></td><td>${data.user.phoneNumber}</td></tr>
                           <tr><td><strong>성별:</strong></td><td>${data.user.gender}</td></tr>
                           <tr><td><strong>주소:</strong></td><td>${data.user.address}</td></tr>
                           <tr><td><strong>가입일:</strong></td><td>${data.formattedDate}</td></tr>
                       </tbody>
                   </table>
                   <a href="/editProfile" class="edit-profile-link">프로필 수정</a>
               </div>
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
                    <div class="wishlist-text">
                        <a href="#" onclick="checkItemBeforeRedirect(${item.itemId}, '${item.itemId}')">
                            <strong class="wishlist-title">${item.itemTitle}</strong>
                        </a>
                        <strong class="wishlist-price">${item.formattedPrice} 원</strong><br><br>
                        <button class="delete-button" onclick="deleteItem(${item.id})">삭제</button>
                    </div>
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

// 구매내역 불러오기
function loadPurchaseList(){
    fetch('/mypage/cart')
        .then(response => response.json())
        .then(data => {
            const purchaseListContainer = document.querySelector('.purchase-history-panel .purchaselist');
            const emptyMessage = document.querySelector('.no-purchase-history-message');
            purchaseListContainer.innerHTML = "";

            if(data.purchaseList.length === 0) {
                emptyMessage.style.display = "block";
            }else {
                emptyMessage.style.display = "none";
                data.purchaseList.forEach(item => {
                    const itemRow = `
                        <div class= >
                        <tr>
                            <td>
                                <a href="/item/${item.itemId}">
                                    <img src="${item.itemImgURL}">
                                </a>
                            </td>
                            <td>
                                <a href="/item/${item.itemId}">
                                    ${item.itemTitle}
                                </a>
                            </td>
                            <td>${item.purchaseDate}</td>
                        </tr>
                    `;
                    soldListContainer.innerHTML += itemRow;
                });
            }
        })
        .catch(error => console.error('Error loading user info: ', error));
};

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
                    <a class="sold-item-box" href="/item/${item.id}">
                            <div class="sold-item-img">
                               <img src="${item.imgURL}">
                            </div>
                            <div>
                                <div class="sold-item-title">
                                <span>${item.title}</span>
                                </div>
                                <div class="sold-item-price">
                                    ${item.formattedPrice} 원
                                </div>
                            </div>
                    </a>
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
    const starStyle = 'font-size: 20px; color: gold; white-space: nowrap; letter-spacing: 0; text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);';

    for (let i = 1; i <= 5; i++) {
        if (i <= rating) {
            // 채워진 별에 스타일 적용
            starsHtml += `<span style="${starStyle}">★</span>`;
        } else {
            // 빈 별에 스타일 적용
            starsHtml += `<span style="${starStyle}">☆</span>`;
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
                            <div style="border-top:1px solid #b1b1b1">${displayRating(item.rating)}</div>
                            <h4 style="border-bottom:1px solid #b1b1b1">${item.content}</h4>
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
                            <h4 style="border-bottom:1px solid #b1b1b1">${item.reviewerName}</h4>
                            <div>${displayRating(item.rating)}</div>
                            <h4>${item.createdAt}</h4>
                            <h4 style="border-bottom:1px solid #b1b1b1">${item.content}</h4>
                        </div>
                    `;
                    reviewRecContainer.innerHTML += itemRow;
                });
            }
        })
        .catch(error => console.error('Error loading reviews: ', error));
};
