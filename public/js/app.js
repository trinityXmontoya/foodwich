$(document).foundation()

$(document).ready(function(){
  $("#address-form").on("submit", function(e){
    var formData = JSON.stringify($(this).serializeArray());
    $.ajax({
      type: "POST",
      url: "/search",
      data: formData,
      dataType: "json",
      contentType: "application/json",
      complete: function(response){
        options = response.responseText
        $("#results").html(options)
      }
    })
    return false;
  });
});
