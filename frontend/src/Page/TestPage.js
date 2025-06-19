

function TestPage() {
    const handleFileUpload = async (e) => {
        const file = e.target.files[0];

        if (!file) {
            return;
        }

        const data = new FormData();
        data.append("file", file)
        data.append("upload_preset", "TheMovie")
        data.append("cloud_name", " do5o9r18f")
        console.log(file);

        const res = await fetch("https://api.cloudinary.com/v1_1/do5o9r18f/image/upload", {
            method: "POST",
            body: data
        });

        const uploadUrl = await res.json();
        console.log(uploadUrl);


    }
    return (
        <div>
            <input type="file" onChange={ handleFileUpload } />
        </div>
    )
}

export default TestPage