
async function fetchFavHotels() {
    let res = await fetch(`/favsdata?act=get`, {method: 'get'});
    let json = await res.json();

    if (json.Error) {
        document.getElementById('fav_hotels_table').innerHTML = json.Error;
        document.getElementById('clear_favs_button').disable = true;
        return;
    }

    let s = '';
    json.fav_hotels.forEach(h => {s += `<tr><td>${h}</td></tr>`});
    document.getElementById('fav_hotels_table').innerHTML = s;
    if (json.fav_hotels.length > 0)
        document.getElementById('clear_favs_button').disabled = false;
}
