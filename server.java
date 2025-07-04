const express = require('express');
const mongoose = require('mongoose');
const multer = require('multer');
const cloudinary = require('cloudinary').v2;
const { CloudinaryStorage } = require('multer-storage-cloudinary');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

const PORT = process.env.PORT || 5000;

// MongoDB connect
mongoose.connect(process.env.MONGO_URI, { useNewUrlParser: true, useUnifiedTopology: true })
.then(() => console.log("MongoDB connected"))
.catch(err => console.log(err));

// Song schema
const SongSchema = new mongoose.Schema({
  title: String,
  artist: String,
  fileUrl: String
});
const Song = mongoose.model('Song', SongSchema);

// Cloudinary config
cloudinary.config({
  cloud_name: process.env.CLOUDINARY_CLOUD_NAME,
  api_key: process.env.CLOUDINARY_API_KEY,
  api_secret: process.env.CLOUDINARY_API_SECRET
});

// Multer + Cloudinary
const storage = new CloudinaryStorage({
  cloudinary: cloudinary,
  params: {
    folder: 'music_uploads',
    resource_type: 'auto'
  }
});
const upload = multer({ storage: storage });

// Routes
app.get('/songs', async (req, res) => {
  const songs = await Song.find();
  res.json(songs);
});

app.post('/upload', upload.single('file'), async (req, res) => {
  const { title, artist } = req.body;
  const fileUrl = req.file.path;

  const song = new Song({ title, artist, fileUrl });
  await song.save();

  res.json({ message: "Song uploaded", song });
});

app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
