
async function fetchHotels() {
    let hotelnamefrag = document.getElementById('hotel_name').value;
    let res = await fetch(`/hoteldata?by=name&query=${hotelnamefrag}`, {method: 'get'});
    let json = await res.json();

    if (json.Error) {
        document.getElementById('hotels_table_title').innerHTML = `No hotels with '${hotelnamefrag}'`;
        document.getElementById('hotels_table').innerHTML = '';
        return;
    }

    document.getElementById('hotels_table_title').innerHTML = `Hotels with '${hotelnamefrag}'`;
    let s = '';
    json.hotels.forEach(h => {
        if (h.fav === true) {
            s += `<tr><td><input id="${h.hotelid}" type="button" class="btn btn-primary btn-sm pull-right" value="Fav &starf;" onclick="clickFavHotel(${h.hotelid})"><a href="/hotel?id=${h.hotelid}"><b>${h.hotelname}</b></a>, ID ${h.hotelid}<br>Rated ${h.rating}<br>Address: ${h.address}<br>Expedia Link:<br><a href="/click?link=${h.link}" target="_blank">${h.link}</a></td></tr>`;
        } else {
            s += `<tr><td><input id="${h.hotelid}" type="button" class="btn btn-default btn-sm pull-right" value="Fav &starf;" onclick="clickFavHotel(${h.hotelid})"><a href="/hotel?id=${h.hotelid}"><b>${h.hotelname}</b></a>, ID ${h.hotelid}<br>Rated ${h.rating}<br>Address: ${h.address}<br>Expedia Link:<br><a href="/click?link=${h.link}" target="_blank">${h.link}</a></td></tr>`;
        }});
    document.getElementById('hotels_table').innerHTML = s;
}
