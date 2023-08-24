
async function fetchNextReviews() {
    let hotelid = document.getElementById('hotelid').value;
    let offset = document.getElementById('offset').value;
    let res = await fetch(`/reviewdata?id=${hotelid}&dir=next&offset=${offset}`, {method: 'get'});
    let json = await res.json();

    // check if error or reviews
    if (json.Error) {
        document.getElementById('reviews_table').innerHTML = `<tr class="text-muted"><td>${json.error}</td></tr>`;
        return;
    }

    let total_reviews = parseInt(json.len);
    if (total_reviews == 0) {
        document.getElementById('reviews_table').innerHTML = `<tr class="text-muted"><td>'No reviews for hotel ${hotelid}'</td></tr>`;
        return;
    }

    let s = '';
    json.reviews.forEach(r => {s += `<tr><td>Review by <b>${r.username}</b> on ${r.dateposted}<br><b>Title</b>:<br>${r.title}<br><b>Body</b>:<br>${r.text}</tr></td>`});
    document.getElementById('reviews_table').innerHTML = s;

    let rest = parseInt(offset, 10) + 10;
    document.getElementById('offset').value = rest;

    if (rest >= 20)
        document.getElementById('prev_button').disabled = false;
    if (rest >= total_reviews)
        document.getElementById('next_button').disabled = true;
}

async function fetchPrevReviews() {
    let hotelid = document.getElementById('hotelid').value;
    let offset = document.getElementById('offset').value;
    let res = await fetch(`/reviewdata?id=${hotelid}&dir=prev&offset=${offset}`, {method: 'get'});
    let json = await res.json();

    // check if error or reviews
    if (json.error) {
        document.getElementById('reviews_table').innerHTML = `<tr class="text-muted"><td>${json.error}</td></tr>`;
        return;
    }

    let total_reviews = parseInt(json.len);
    if (total_reviews == 0) {
        document.getElementById('reviews_table').innerHTML = `<tr class="text-muted"><td>'No reviews for hotel ${hotelid}'</td></tr>`;
        return;
    }

    let s = '';
    json.reviews.forEach(r => {s += `<tr><td>Review by <b>${r.username}</b> on ${r.dateposted}<br><b>Title</b>:<br>${r.title}<br><b>Body</b>:<br>${r.text}</tr></td>`});
    document.getElementById('reviews_table').innerHTML = s;

    let rest = parseInt(offset, 10) - 10;
    document.getElementById('offset').value = rest;

    if (rest < total_reviews)
        document.getElementById('next_button').disabled = false;
    if (rest <= 10)
        document.getElementById('prev_button').disabled = true;
}
