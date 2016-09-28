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
