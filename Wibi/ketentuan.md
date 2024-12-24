cara menggunakan : 

1. pastikan sudah berada pada directory ../jarkom/wibi
    jika belum, maka dapat melakukan change directory ke wibi

2. lakukan command berikut pada terminal (disarankan menggunakan bash) : 
        check apakah sudah ada node atau npm pada devices dengan mengetik : node -v atau npm -v
        versi minimal untuk node yang digunakan adalah v.20.17.0, jika lebih rendah maka dapat memperbarui terlebih dahulu
            jika tidak ada node maka dapat menginstal node terlebih dahulu dan mengonfigurasikannya dengan perangkat.
                berikut tutorial install node : [install node](https://youtu.be/06X51c6WHsQ?si=x-6-JAyvzR5cMdQH)

3. jalankan npm init -y 

4. jalankan npm install express socket.io

5. lalu jalankan node server.js -> untuk menjalankan server, jika sudah maka
    untuk mengakses halaman client dapat mengunjungi http://localhost:3000 (dapat diakses diberbagai browser, asalkan masih satu devices dengan server)
    untuk mengakses halaman host dapat mengunjungi http://localhost:3000/start-quiz


on going (gak pasti ada) :
1. soal pake API (paling diusahain biar line di server gak banyak, dan bisa custom soal dengan mudah)
2. styling quiznya (agak mager karna dah cape debug)
3. deploy (diusahain kalau bisa yah hehe 🥰🥰)
