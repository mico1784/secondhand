 document.querySelector('.search-btn').addEventListener('submit', function(e) {
        e.preventDefault();
        var searchText = document.getElementById('searchText').value;
        if (searchText) {
            this.action = '/list/' + encodeURIComponent(searchText);
            this.submit();
        }
    });

