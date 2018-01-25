package com.dalseonggun.DalseongTour;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.kakao.auth.AuthType;
import com.kakao.auth.ErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.KakaoSDK;
import com.kakao.auth.Session;
import com.kakao.kakaostory.callback.StoryResponseCallback;
import com.kakao.kakaostory.request.PostRequest;
import com.kakao.kakaostory.response.model.MyStoryInfo;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.data.OAuthErrorCode;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import com.kakao.kakaostory.KakaoStoryService;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends UnityPlayerActivity {

    static String TAG = "[JH_Unity] Social";

    public static String GameObjectName;

    boolean isCheckingStoryUser = false;

    private OAuthLogin mOAuthLoginModule;

    final String SUCCESS = "Success";
    final String FAIL = "Fail";

    final String Method_OnInitPlugin_Kakao = "OnInitPlugin_Kakao";
    final String Method_OnInitPlugin_Naver = "OnInitPlugin_Naver";
    final String Method_LoginResult = "OnLoginResult";
    final String Method_LogoutResult ="OnLogout";
    final String Method_StoryUserResult = "OnStoryUserResult";
    final String Method_UploadPhotoResult = "OnRequestPostPhoto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //간편로그인시 호출 ,없으면 간편로그인시 로그인 성공화면으로 넘어가지 않음
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            Log.d("Unity-onActivityResult", "간편로그인 / " + requestCode + " / " + resultCode + " / " + data);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void Init(String gameObjectName){
        GameObjectName = gameObjectName;
    }

    /**
     * 카카오 SDK를 초기화 한다
     */
    public void InitKakao()
    {
        Log.d(TAG, "Init Kakao SDK Adapter.\n>>Register session callback");

        //카카오 SDK 초기화
        com.kakao.auth.KakaoSDK.init(new KakaoSDKAdapter());
        Session.getCurrentSession().addCallback(new SessionCallback());
        UnityPlayer.UnitySendMessage(GameObjectName, Method_OnInitPlugin_Kakao, SUCCESS);
    }

    /**
     * 네이버 로그인 모듈을 초기화 한다
     * @param clientID 애플리케이션 등록 후 발급받은 클라이언트 아이디
     * @param clientSecret 애플리케이션 등록 후 발급받은 클라이언트 시크릿
     * @param clientName 네이버 앱의 로그인 화면에 표시할 애플리케이션 이름
     */
    public void InitNaver(String clientID, String clientSecret, String clientName)
    {
        Log.d(TAG, "Init Naver module. >> ClientID: " + clientID + ", Client Name: " + clientName);

        //네이버 SDK 초기화
        mOAuthLoginModule = OAuthLogin.getInstance();
        mOAuthLoginModule.init(
                UnityPlayer.currentActivity.getApplicationContext()
                , clientID //"MYHYl06kG_DV5Xtribhy"
                , clientSecret //"nvVIrHz2aW"
                , clientName // "com.dalseonggun.dalseongtour"
        );

        UnityPlayer.UnitySendMessage(GameObjectName, Method_OnInitPlugin_Naver, SUCCESS);
    }

    /**
     * 카카오 로그인
     */
    public void LoginKakao() {
        Log.d(TAG, "Login - kakao talk");

        runOnUiThread(new Runnable() {
            public void run()
            {
                Session.getCurrentSession().open(AuthType.KAKAO_TALK, UnityPlayer.currentActivity);
            }
        });
    }

    /**
     * 카카오 로그아웃
     */
    public void LogoutKakao(){
        Log.d(TAG, "Logout - kakao talk");

        UserManagement.requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                Log.d("Unity KakaoTest", "로그아웃");
                UnityPlayer.UnitySendMessage(GameObjectName, Method_LogoutResult, "Logout");
            }
        });
    }

    // 카카오 스토리 가입자인지 체크
    public void CheckIsStoryUser() {

        KakaoStoryService.requestIsStoryUser(new KakaoStoryResponseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Log.d(TAG, "requestIsStoryUser() : " + result);
                UnityPlayer.UnitySendMessage(GameObjectName, Method_StoryUserResult, result == true ? SUCCESS : FAIL);
            }
        });
    }

    public void RequestPostPhoto(String imagePath, String content) {
        try {

            List<File> fileList = new ArrayList<File>();
            final File uploadFile = new File(imagePath);
            fileList.add(uploadFile);

            KakaoStoryService.requestPostPhoto(new KakaoStoryResponseCallback<MyStoryInfo>() {
                @Override
                public void onSuccess(MyStoryInfo result) {
                    Log.i(TAG, "Success request post photo -> posting ID : " + result.getId().toString());
                    UnityPlayer.UnitySendMessage(GameObjectName, Method_UploadPhotoResult, SUCCESS);
                }

                @Override
                public void onDidEnd() {

                }
            }, fileList, content, PostRequest.StoryPermission.PUBLIC, true, null, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void RequestPostLink(String link, String linkContent) {
        try {
            KakaoStoryService.requestPostLink(new KakaoStoryPostingResponseCallback<MyStoryInfo>() {
                @Override
                public void onSuccess(MyStoryInfo result) {
                    Log.i(TAG, "Success request post link -> posting ID : " + result.getId().toString());
                    UnityPlayer.UnitySendMessage(GameObjectName, Method_UploadPhotoResult, SUCCESS);
                }
            }, link, linkContent);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void LoginNaver(){
        Log.d("Unity Naver", "Naver 로그인 시도");
        mOAuthLoginModule.startOauthLoginActivity(UnityPlayer.currentActivity, mOAuthLoginHandler);
    }

    public void LogoutNaver(){
        Log.d("Unity Naver", "Naver 로그아웃");
        mOAuthLoginModule.logout(UnityPlayer.currentActivity);
        UnityPlayer.UnitySendMessage(GameObjectName, Method_LogoutResult, "Logout");
    }


    private LoginErrorCode ConvertKakaoCodeToCommonErrorCode(ErrorResult errorResult, String errorMsg)
    {
        Log.d(TAG, errorMsg + " Error Result =>\n " + errorResult.toString());
        ErrorCode errorCode = ErrorCode.valueOf(errorResult.getErrorCode());

        LoginErrorCode myErrorCode = LoginErrorCode.NONE;

        switch (errorCode)
        {
            // 서버 연결 에러
            case AUTH_ERROR_CODE:
            case UNDEFINED_ERROR_CODE:
            case INVALID_TOKEN_CODE:
            case NOT_EXIST_APP_CATEGORY_CODE:
            case NOT_EXIST_APP_CODE:
            case KAKAO_MAINTENANCE_CODE:
                myErrorCode = LoginErrorCode.SERVER_ERRROR;
                break;

            // 권한 없음
            // 나이/연령 등 모든 제한
            case NOT_SUPPORTED_API_CODE:
            case BLOCKED_ACTION_CODE:
            case ACCESS_DENIED_CODE:
            case EXCEED_LIMIT_CODE:
            case INVALID_SCOPE_CODE:
            case NEED_TO_AGE_AUTHENTICATION:
            case UNDER_AGE_LIMIT:
                myErrorCode = LoginErrorCode.NO_AUTHORIZED;
                break;

            // Client 연결 실패
            case CLIENT_ERROR_CODE:
                myErrorCode = LoginErrorCode.CONNECTION_ERROR;
                break;

            // 잘못된 데이터 전달
            case INTERNAL_ERROR_CODE:
            case INVALID_PARAM_CODE:
                myErrorCode = LoginErrorCode.INVALID_REQUEST;
                break;

            // 등록되지 않은 사용자
            case NOT_REGISTERED_USER_CODE:
            case NOT_EXIST_KAKAO_ACCOUNT_CODE:
            case NOT_EXIST_KAKAOTALK_USER_CODE:
            case NOT_EXIST_KAKAOSTORY_USER_CODE:
                myErrorCode = LoginErrorCode.NO_REGISTERED_USER;
                break;
        }

        return  myErrorCode;
    }

    private LoginErrorCode ConvertNaverCodeToCommonErrorCode(OAuthErrorCode errorCode)
    {
        LoginErrorCode myErrorCode = LoginErrorCode.NONE;

        switch (errorCode)
        {
            // 서버 에러
            case SERVER_ERROR_SERVER_ERROR:
            case SERVER_ERROR_UNSUPPORTED_RESPONSE_TYPE:
            case SERVER_ERROR_INVALID_SCOPE:
            case SERVER_ERROR_TEMPORARILY_UNAVAILABLE:
                myErrorCode = LoginErrorCode.SERVER_ERRROR;
                break;

            //  접근 권한 없음
            case SERVER_ERROR_UNAUTHORIZED_CLIENT :
            case SERVER_ERROR_ACCESS_DENIED:
            case CLIENT_ERROR_CERTIFICATION_ERROR:
                myErrorCode = LoginErrorCode.NO_AUTHORIZED;
                break;

            // 연결 실패
            case CLIENT_ERROR_CONNECTION_ERROR:
                myErrorCode = LoginErrorCode.CONNECTION_ERROR;
                break;

            // 전달된 데이터 잘못됨
            case SERVER_ERROR_INVALID_REQUEST:
            case CLIENT_ERROR_PARSING_FAIL:
            case CLIENT_ERROR_NO_CLIENTID:
            case CLIENT_ERROR_NO_CLIENTSECRET:
            case CLIENT_ERROR_NO_CLIENTNAME:
            case CLIENT_ERROR_NO_CALLBACKURL:
                myErrorCode = LoginErrorCode.INVALID_REQUEST;
                break;

            // 사용자 취소
            case CLIENT_USER_CANCEL:
                myErrorCode = LoginErrorCode.USER_CANCEL;
                break;
        }

        return  myErrorCode;
    }

    //네이버 로그인 결과값
    private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if (success) {
                Log.e("Unity-Naver", "로그인 성공");

                new Thread() {
                    public void run() {
                        String token = mOAuthLoginModule.getAccessToken(UnityPlayer.currentActivity);
                        String info = mOAuthLoginModule.requestApi(UnityPlayer.currentActivity, token, "https://openapi.naver.com/v1/nid/me");

                        String id = null;

                        try {
                            id = (String)((JSONObject)(((JSONObject) new JSONParser().parse(info)).get("response"))).get("id");

                            Log.d(TAG, "naver login success() : ID -> " + id);
                            UnityPlayer.UnitySendMessage(GameObjectName, Method_LoginResult, SUCCESS + ":" + id);

                        } catch (Exception e) {
                            Log.e(TAG, "Exception on parsing result.\n" + e.getMessage());
                            UnityPlayer.UnitySendMessage(GameObjectName, Method_LoginResult, FAIL);
                        }
                    }
                }.start();

            } else {
                Log.e("Unity-Naver", "실패");
                OAuthErrorCode errorCode = mOAuthLoginModule.getLastErrorCode(UnityPlayer.currentActivity.getApplicationContext());
                UnityPlayer.UnitySendMessage(GameObjectName, Method_LoginResult, ConvertNaverCodeToCommonErrorCode(errorCode).toString());
            }
        };
    };

    //카카오 로그인 결과값
    /**
     * 카카오 로그인 Callback
     */
    private class SessionCallback implements ISessionCallback {

        // access token을 성공적으로 발급 받아 valid access token을 가지고 있는 상태.
        // 일반적으로 로그인 후의 다음 activity로 이동한다
        @Override
        public void onSessionOpened() {
            UserManagement.requestMe(new MeResponseCallback() {

                @Override
                public void onFailure(ErrorResult errorResult) {

                    UnityPlayer.UnitySendMessage(GameObjectName, Method_LoginResult, ConvertKakaoCodeToCommonErrorCode(errorResult, "onFailure()").toString());
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {

                    UnityPlayer.UnitySendMessage(GameObjectName, Method_LoginResult, ConvertKakaoCodeToCommonErrorCode(errorResult, "onSessionClosed()").toString());
                }

                @Override
                public void onNotSignedUp() {

                    UnityPlayer.UnitySendMessage(GameObjectName, Method_LoginResult, LoginErrorCode.NO_REGISTERED_USER.toString());
                }

                @Override
                public void onSuccess(UserProfile userProfile) {

                    //로그인에 성공하면 로그인한 사용자의 일련번호, 닉네임, 이미지url등을 리턴합니다.
                    //사용자 ID는 보안상의 문제로 제공하지 않고 일련번호는 제공합니다.
                    Log.e(TAG, "kakao login onSuccess() : -> " + "ID: " +userProfile.getId() + "\nNick name: " + userProfile.getNickname());

                    // 카카오스토리 사용자인지 체크 중에 세션이 만료되어
                    // 로그인요청을 한 경우 스토리 사용자인지 다시 확인해본다

                    if(isCheckingStoryUser)
                    {
                        CheckIsStoryUser();
                        isCheckingStoryUser = false;
                    }
                    else
                    {
                        UnityPlayer.UnitySendMessage(GameObjectName, Method_LoginResult, SUCCESS + ":" + userProfile.getId());
                    }
                }
            });
        }

        // memory와 cache에 session 정보가 전혀 없는 상태.
        // 일반적으로 로그인 버튼이 보이고 사용자가 클릭시 동의를 받아 access token 요청을 시도한다.

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Log.d(TAG, "onSessionOpenFailed : " + exception);
        }
    }

    private abstract class KakaoStoryResponseCallback<T> extends StoryResponseCallback<T> {

        @Override
        public void onNotKakaoStoryUser() {
            Log.i(TAG, "not KakaoStory user");
            UnityPlayer.UnitySendMessage(GameObjectName, Method_StoryUserResult, LoginErrorCode.NO_KAKAOSTORY_USER.toString());
        }

        @Override
        public void onFailure(ErrorResult errorResult) {
            UnityPlayer.UnitySendMessage(GameObjectName, Method_StoryUserResult, ConvertKakaoCodeToCommonErrorCode(errorResult, "KakaoStoryResponseCallback : onFailure()").toString());
        }

        @Override
        public void onSessionClosed(ErrorResult errorResult) {
            Log.i(TAG, "onSessionClosed : " + errorResult + "\nTry to kakao login. set variable isCheckingStoryUser = true");
            isCheckingStoryUser = true;
            LoginKakao();
        }

        @Override
        public void onNotSignedUp() {
            UnityPlayer.UnitySendMessage(GameObjectName, Method_StoryUserResult, LoginErrorCode.NO_REGISTERED_USER.toString());
        }
    }

    private abstract class KakaoStoryPostingResponseCallback<T> extends StoryResponseCallback<T> {

        @Override
        public void onNotKakaoStoryUser() {
            Log.i(TAG, "not KakaoStory user");
            UnityPlayer.UnitySendMessage(GameObjectName, Method_UploadPhotoResult, LoginErrorCode.NO_KAKAOSTORY_USER.toString());
        }

        @Override
        public void onFailure(ErrorResult errorResult) {
            UnityPlayer.UnitySendMessage(GameObjectName, Method_UploadPhotoResult, ConvertKakaoCodeToCommonErrorCode(errorResult, "KakaoStoryResponseCallback : onFailure()").toString());
        }

        @Override
        public void onSessionClosed(ErrorResult errorResult) {
            Log.i(TAG, "onSessionClosed : " + errorResult + "\nTry to kakao login. set variable isCheckingStoryUser = true");
            isCheckingStoryUser = true;
            LoginKakao();
        }

        @Override
        public void onNotSignedUp() {
            UnityPlayer.UnitySendMessage(GameObjectName, Method_UploadPhotoResult, LoginErrorCode.NO_REGISTERED_USER.toString());
        }
    }


    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }
}
