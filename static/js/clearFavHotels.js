
async function clearFavHotels() {
    let res = await fetch(`/favsdata?act=clear`, {method: 'get'});
    let json = await res.json();

    document.getElementById('fav_hotels_table').innerHTML = '';
    document.getElementById('clear_favs_button').disabled = true;
}
