import { GoogleLogin, GoogleOAuthProvider } from '@react-oauth/google';
import { handleLoginGoogleApi } from '../../service/AuthService';
import { useContext } from 'react';
import { AuthContext } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const GOOGLE_CLIENT_ID = "1085883031350-7ihbulo2h3oure1c75sv8rc939b89rl4.apps.googleusercontent.com";
function GoogleBtn() {
    const { login } = useContext(AuthContext);
    const navigate = useNavigate();

    const handleSuccess = async (credentialResponse) => {
        const idToken = credentialResponse.credential;

        try {
            const response = await handleLoginGoogleApi(idToken);
            console.log("acc data: ", response);

            login(response.data.result.account, response.data.result.token, response.data.result.refresToken);
            navigate('/');
        } catch (error) {
            console.log(error)
        }
    };

    return (
        <GoogleOAuthProvider clientId={ GOOGLE_CLIENT_ID }>
            <GoogleLogin
                onSuccess={ handleSuccess }
                onError={ () => console.log("Google Login thất bại") }
            />
        </GoogleOAuthProvider>
    );
};

export default GoogleBtn