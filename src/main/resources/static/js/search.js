 document.getElementById('searchForm').addEventListener('submit', function(e) {
        e.preventDefault();
        var searchText = document.getElementById('searchText').value;
        if (searchText) {
            this.action = '/list/' + encodeURIComponent(searchText);
            this.submit();
        }
    });

     document.getElementById('searchForm').addEventListener('submit', function(e) {
      e.preventDefault();
      var searchText = document.getElementById('searchText').value;
      if (searchText) {
          this.action = '/list/' + encodeURIComponent(searchText);
          this.submit();
      }
  });
