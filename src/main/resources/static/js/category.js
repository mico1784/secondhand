      function updateSubcategories() {
            const category = document.getElementById("category").value;
            const subcategorySelect = document.getElementById("subcategory");
            subcategorySelect.innerHTML = "";

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
        }