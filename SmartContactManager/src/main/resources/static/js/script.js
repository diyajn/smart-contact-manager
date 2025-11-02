console.log("this is script file");

const toggleSidebar = () => {
  if ($(".sidebar").is(":visible")) {
    //true
    //bnd krna h
    $(".sidebar").css("display", "none");
    $(".content").css("margin-left", "0%");
  } else {
    //false
    //show krna h
    $(".sidebar").css("display", "block");
    $(".content").css("margin-left", "20%");
  }
};

const search = () => {
  //console.log("searching...");
  let keyword = $("#search-input").val();
  if (keyword == "") {
    $(".search-result").hide();
  } else {
    //search
    console.log(keyword);
    //sending request to server
    let url = `http://localhost:8080/search/${keyword}`;

    fetch(url)
      .then((response) => {
        return response.json();
      })
      .then((data) => {
        console.log(data);
        let text = `<div class='list-group'>`;
        data.forEach((contact) => {
         text += `<a href='/user/contact/${contact.cId}' class='list-group-item list-group-item-action'>${contact.name}</a>`;
        });

        text += `</div>`;

            $(".search-result").html(text);
            $(".search-result").show();
      });
  }
};
