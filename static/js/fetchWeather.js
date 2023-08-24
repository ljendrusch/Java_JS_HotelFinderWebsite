
async function fetchWeather() {
    let hotelid = document.getElementById('hotelid').value;
    let lat = document.getElementById('lat').value;
    let lng = document.getElementById('lng').value;
    let res = await fetch(`https://api.open-meteo.com/v1/forecast?current_weather=true&temperature_unit=fahrenheit&windspeed_unit=mph&latitude=${lat}&longitude=${lng}`, {method: 'get'});
    let json = await res.json();

    let s = `Current Weather:<br>Temperature: ${json.current_weather.temperature} f<br>Windspeed: ${json.current_weather.windspeed} mph<br>`;
    let wCode = parseInt(json.current_weather.weathercode);
    if (wCode == 0) {
        s += `WMO Code: 0, Clear Skies`;
    } else if (wCode < 4) {
        s += `WMO Code: ${wCode}, Overcast`;
    } else {
        s += `WMO Code: ${wCode}, Inclement Conditions`;
    }

    document.getElementById('hotel_weather').innerHTML = s;
}
