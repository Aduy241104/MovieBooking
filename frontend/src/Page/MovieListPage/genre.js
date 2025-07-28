const GENRES = [
    "Hành động",
    "Tình cảm - Lãng mạn",
    "Tâm lý",
    "Kinh dị",
    "Phiêu lưu",
    "Hoạt hình",
    "Hài hước",
    "Chiến tranh",
    "Khoa học viễn tưởng"
    // ... thêm hoặc xóa dễ dàng
];


const genreMenu = {
    items: [
        {
            key: "All",
            label: <div onClick={ () => console.log("")}>Tất cả</div>,
        },
        ...GENRES.map((genre) => ({
            key: genre,
            label: <div onClick={ () => console.log("") }>{ genre }</div>,
        })),
    ],
};

export default genreMenu;