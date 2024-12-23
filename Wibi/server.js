const express = require("express");
const http = require("http");
const { Server } = require("socket.io");

const app = express();
const server = http.createServer(app);
const io = new Server(server);

const PORT = 3000;

let players = [];
let leaderboard = [];
let waitingRoom = [];
let quizStarted = false;
let host = null;
let quizTimer = null; // Timer untuk batas waktu kuis (2 menit)
const QUIZ_DURATION = 120000; // 2 menit dalam milidetik

// Data Quiz
const questions = [
  {
    id: 1,
    question:
      "Di dalam sebuah router, HOL (Head-of-the-Line) Blocking disebabkan oleh?",
    options: [
      "Queueing delay dan kehilangan paket di sebuah input buffer",
      "Queueing delay dan kehilangan paket di sebuah output buffer",
      "Saling menghalangi paket-paket yang berada di input buffer yang berbeda",
      "Saling menghalangi paket-paket yang berada di output buffer yang berbeda",
    ],
    answer:
      "Saling menghalangi paket-paket yang berada di input buffer yang berbeda",
  },
  {
    id: 2,
    question:
      "Probabilitas terjadinya paling sedikit dua bit error ketika 𝑘 bit ditransmisikan dengan probabilitas bit error 𝑝 adalah?",
    options: [
      "1−(1−𝑝)^𝑘",
      "1−(1−𝑝)^𝑘 −𝑘𝑝(1−𝑝)^{𝑘−1}",
      "1−(1−𝑝)^{𝑘+1} −𝑝(1−𝑝)^𝑘",
      "𝑝",
    ],
    answer: "1−(1−𝑝)^𝑘 −𝑘𝑝(1−𝑝)^{𝑘−1}",
  },
  {
    id: 3,
    question: "Yang mana di antara yang berikut yang bukan layanan link-layer?",
    options: [
      "Flow control",
      "Error correction",
      "Reliable data transfer",
      "Congestion control",
    ],
    answer: "Congestion control",
  },
  {
    id: 4,
    question: "Berapa segment dipertukarkan untuk membuat sebuah koneksi TCP?",
    options: ["1 segment", "2 segment", "3 segment", "Tidak ada"],
    answer: "3 segment",
  },
  {
    id: 5,
    question:
      "Pernyataan mana tentang error detection/correction berikut yang salah?",
    options: [
      "Error detection hanya diimplementasikan di link-layer",
      "Metode deteksi dengan polynomial menambahkan data redundan di akhir message",
      "Dengan CRC kita bisa membenarkan bit error yang jumlahnya ganjil",
      "Parity check dapat digunakan untuk membenarkan satu bit error",
    ],
    answer:
      "Metode deteksi dengan polynomial menambahkan data redundan di akhir message",
  },
  {
    id: 6,
    question:
      "Berapa banyak interface yang harus dilewati oleh IP datagram ketika melewati 3 router di antara sumber A dan tujuan B?",
    options: ["8", "6", "5", "3"],
    answer: "8",
  },
  {
    id: 7,
    question:
      "Dalam routing antara Autonomous System (AS), isu yang mana yang paling dominan dalam membuat keputusan routing?",
    options: [
      "Jarak antara AS secara geografis",
      "Policy",
      "Banyaknya AS yang mesti dilewati",
      "Level congestion yang terjadi dalam AS-nya",
    ],
    answer: "Policy",
  },
  {
    id: 8,
    question:
      "Berapa persen isi dari IP datagram yang merupakan data aplikasi jika aplikasi membuat paket berisi 60 byte data yang dibungkus dalam segment TCP dan IP datagram?",
    options: ["60%", "80%", "40%", "20%"],
    answer: "60%",
  },
  {
    id: 9,
    question: "Apakah total interface selalu 2^n dengan n router?",
    options: [
      "Iya! Total interface selalu 2^n untuk n router",
      "Tidak! Total interface selalu 2n untuk setiap n router",
      "Tidak! Total interface selalu 2n + 2 untuk setiap n router",
      "Tidak! Total interface selalu n untuk setiap n router",
    ],
    answer: "Tidak! Total interface selalu 2n + 2 untuk setiap n router",
  },
  {
    id: 10,
    question: "Congestion control merupakan layanan dari link?",
    options: [
      "Link layer",
      "Transport layer",
      "Application layer",
      "Network layer",
    ],
    answer: "Transport layer",
  },
];

app.get("/", (req, res) => {
  res.sendFile(__dirname + "/client.html"); // Halaman client atau player
});

app.get("/start-quiz", (req, res) => {
  res.sendFile(__dirname + "/start-quiz.html"); // Halaman khusus host
});

io.on("connection", (socket) => {
  console.log("A player connected:", socket.id);

  socket.on("join_game", (playerName) => {
    if (quizStarted || waitingRoom.length >= 20) return;

    const newPlayer = { id: socket.id, name: playerName, score: 0, time: 0 };
    players.push(newPlayer);
    waitingRoom.push(newPlayer);
    io.emit("update_waiting_room", waitingRoom);
  });

  // Host menetapkan dirinya sebagai host (ini default, tapi kalau mau diubah bisa, tapi agak mager)
  socket.on("set_host", () => {
    if (!host) {
      host = socket.id;
      console.log("Host set:", host);
      socket.emit("host_confirmed"); // Konfirmasi ke host bahwa ia adalah host
    }
  });

  // Host memulai kuis
  socket.on("start_quiz", () => {
    if (socket.id === host && !quizStarted) {
      quizStarted = true;
      io.emit("quiz_started", questions); // Mulai kuis untuk semua pemain
      console.log("Quiz dimulai oleh host.");

      // Set timer untuk 2 menit
      quizTimer = setTimeout(() => {
        console.log("Quiz time ended.");
        quizStarted = false;

        // Kumpulkan jawaban dari pemain yang belum submit
        players.forEach((player) => {
          if (player.score === 0) {
            // Beri skor nol untuk pemain yang tidak submit
            player.score = 0;
            player.time = QUIZ_DURATION / 1000; // Waktu penuh 2 menit
          }
        });

        // Hitung dan kirim leaderboard
        leaderboard = [...players]
          .sort((a, b) => b.score - a.score || a.time - b.time)
          .slice(0, 10);

        io.emit("update_leaderboard", leaderboard);
      }, QUIZ_DURATION);
    }
  });

  // Konversi detik ke format menit:detik
  function formatTime(seconds) {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds.toString().padStart(2, "0")}`;
  }

  socket.on("submit_answers", (data) => {
    const { answers, time } = data;
    console.log(
      `Player ${socket.id} mengirimkan jawaban dengan waktu: ${time}s`
    ); // Debugging tapi bisa dihapus nanti

    const player = players.find((p) => p.id === socket.id);

    if (player) {
      let score = 0;
      answers.forEach((ans, idx) => {
        if (questions[idx] && questions[idx].answer === ans) score++;
      });

      player.score = score;
      player.time = time || QUIZ_DURATION / 1000; // Default waktu jika tidak dikirim

      leaderboard = [...players]
        .sort((a, b) => b.score - a.score || a.time - b.time)
        .slice(0, 10);

      // Format waktu dalam menit:detik sebelum mengirim leaderboard
      const formattedLeaderboard = leaderboard.map((player) => ({
        ...player,
        formattedTime: formatTime(player.time), // Tambahkan waktu yang diformat
      }));

      io.emit("update_leaderboard", formattedLeaderboard);
    }
  });

  socket.on("disconnect", () => {
    players = players.filter((p) => p.id !== socket.id);
    waitingRoom = waitingRoom.filter((p) => p.id !== socket.id);

    // Jika host keluar, host di-reset
    if (socket.id === host) {
      host = null;
      clearTimeout(quizTimer);
      quizStarted = false;
    }

    io.emit("update_waiting_room", waitingRoom);
    console.log("Player disconnected:", socket.id);
  });
});

server.listen(PORT, () => {
  console.log(`Server is running on http://localhost:${PORT}`);
  //untuk masuk ke tampilan host bisa ke http://localhost:3000/start-quiz
});
