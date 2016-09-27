var autocomplete;

function setFormVals(){
  var place = autocomplete.getPlace();
  var addr = place.formatted_address;
  var zip = place.address_components.filter(
    function(x){
      return x.types[0] == "postal_code"
    }
  )[0].short_name;
  var geom = place.geometry.location;
  var coords = geom.lat() + "," + geom.lng();
  document.getElementById('address-input').value = addr;
  document.getElementById('zip-input').value = zip;
  document.getElementById('coords-input').value = coords;
}

function initAutocomplete() {
  // Create the autocomplete object, restricting the search to geographical
  // location types.
  autocomplete = new google.maps.places.Autocomplete(
      (document.getElementById('address-input')),
      {types: ['geocode']});
  autocomplete.addListener('place_changed', setFormVals);
}

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
