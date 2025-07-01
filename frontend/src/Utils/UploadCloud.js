import axios from "axios";

export const uploadCloud = async (data) => {
    if (!data) {
        return;
    }
    const uploadData = new FormData();
    uploadData.append("file", data);
    uploadData.append("upload_preset", "TheMovie");
    uploadData.append("cloud_name", " do5o9r18f");
    const response = await axios.post("https://api.cloudinary.com/v1_1/do5o9r18f/image/upload", uploadData);
    return response.data;

}

export default uploadCloud;