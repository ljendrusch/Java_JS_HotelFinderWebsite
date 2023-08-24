
async function fetchHotel() {
    let hotelid = document.getElementById('hotelid').value;
    let res = await fetch(`/hoteldata?by=id&query=${hotelid}`, {method: 'get'});
    let json = await res.json();

    if (json.Error) {
        document.getElementById('hotel_panel').innerHTML = `No hotel found with ID ${hotelid}`;
        return;
    }

    let h = json.hotels[0];
    let s;
    if (h.fav === true) {
        s = `<tr><td><input id="${h.hotelid}" type="button" class="btn btn-primary btn-sm pull-right" value="Fav &starf;" onclick="clickFavHotel(${h.hotelid})"><h5><a href="/hotel?id=${h.hotelid}"><b>${h.hotelname}</b></a>, ID ${h.hotelid}</h5><h5>Rated ${h.rating}</h5><h5>Address: ${h.address}</h5><h5>Expedia Link:</h5><h5><a href="/click?link=${h.link}" target="_blank">${h.link}</a></h5></td></tr>`;
    } else {
        s = `<tr><td><input id="${h.hotelid}" type="button" class="btn btn-default btn-sm pull-right" value="Fav &starf;" onclick="clickFavHotel(${h.hotelid})"><h5><a href="/hotel?id=${h.hotelid}"><b>${h.hotelname}</b></a>, ID ${h.hotelid}</h5><h5>Rated ${h.rating}</h5><h5>Address: ${h.address}</h5><h5>Expedia Link:</h5><h5><a href="/click?link=${h.link}" target="_blank">${h.link}</a></h5></td></tr>`;
    }
    document.getElementById('lat').value = h.lat;
    document.getElementById('lng').value = h.lng;
    document.getElementById('hotel_panel').innerHTML = s;
}
