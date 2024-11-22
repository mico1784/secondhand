function initMap() {
      var container = document.getElementById('map');
      if (!container) {
        console.error("Map container not found");
        return;
      }

      var options = {
        center: new kakao.maps.LatLng(33.450701, 126.570667), // 기본 위치
        level: 3
      };
      var map = new kakao.maps.Map(container, options);

      // Geolocation API 사용하여 현재 위치 가져오기
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function(position) {
          var lat = position.coords.latitude;
          var lon = position.coords.longitude;
          var currentPosition = new kakao.maps.LatLng(lat, lon);

          // 현재 위치로 지도 중심 변경
          map.setCenter(currentPosition);

          // 현재 위치에 마커 표시
          var marker = new kakao.maps.Marker({
            position: currentPosition
          });
          marker.setMap(map);
        }, function(error) {
          console.error("Geolocation error: " + error.message);

          // 위치 정보를 가져오지 못했을 때 기본 위치로 설정
          alert("위치 정보를 가져오는 데 실패했습니다. 기본 위치로 설정합니다.");
          var defaultPosition = new kakao.maps.LatLng(33.450701, 126.570667);
          map.setCenter(defaultPosition);
        });
      } else {
        console.error("Geolocation is not supported by this browser.");
        alert("현재 브라우저에서 위치 정보 서비스를 지원하지 않습니다.");
      }
    }

    document.addEventListener("DOMContentLoaded", function() {
      kakao.maps.load(initMap);
    });