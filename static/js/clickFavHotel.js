
async function clickFavHotel(hotelid) {
    let res = await fetch(`/favsdata?act=click&id=${hotelid}`, {method: 'get'});
    let json = await res.json();

    if (json.Error) {
        return;
    }

    if (json.fav === true) {
        document.getElementById(`${hotelid}`).setAttribute('class', "btn btn-primary btn-sm pull-right");
    } else {
        document.getElementById(`${hotelid}`).setAttribute('class', "btn btn-default btn-sm pull-right");
    }
}
