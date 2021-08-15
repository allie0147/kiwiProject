# Kiwi Market
중고거래 어플리케이션 "당근마켓" 클론코딩
> 2020/08/21 ~ 2020/09/27   
위치(동네) 기반 중고 거래 서비스 및 1:1 채팅 시스템 구현   

<img src="https://user-images.githubusercontent.com/62925639/95828740-4defee00-0d70-11eb-80cf-ec0e24c80e14.jpg" alt="kiwiMarket" width="15%" height="15%" />

## 설치하기 전에..
- Firebase에 Android Project 등록
- **Authentication** 탭에서 Sign-in Method **Email/Password,Phone,Google** 활성화
- **Google Maps Android API key** 발급    

## 개발환경
**SDK** *Android Studio, Firebase*    
**Language** *Java 1.8*    
**Android Sdk Version** *minSdkVersion 16, compileSdkVersion 30*    
**Database** *Firebase Realtime Database, Firebase Storage*    
**API** *Google Maps for Android*    
**Build** *Gradle*    
**Library**    
*Ted Permission* (<https://github.com/ParkSangGwon/TedPermission>)    
*Glide* (<https://github.com/bumptech/glide>)    
*android-ago* (<https://github.com/curioustechizen/android-ago>)    
*PageIndicatiorView* (<https://github.com/romandanylyk/PageIndicatorView>)    
*CricleImageView* (<https://github.com/hdodenhof/CircleImageView>)    
*Shimmer* (<https://github.com/facebook/shimmer-android/>)

## 사용 예제
*Login*     
<img src="https://user-images.githubusercontent.com/62925639/95828712-492b3a00-0d70-11eb-8fa2-b7a7b4048487.jpg" alt="login" width="15%" height="15%" />

<br>*Sign-up*     
<img src="https://user-images.githubusercontent.com/62925639/95828738-4cbec100-0d70-11eb-9a82-c1d080210377.jpg" alt="signUp" width="15%" height="15%" /> <img src="https://user-images.githubusercontent.com/62925639/95828739-4d575780-0d70-11eb-85a0-218373eae0c2.jpg" alt="signUp_error" width="15%" height="15%" />

<br>*Forget Email*    
<img src="https://user-images.githubusercontent.com/62925639/95828707-4892a380-0d70-11eb-81f0-3d35341df4a5.jpg" alt="forget_email" width="15%" height="15%" /> <img src="https://user-images.githubusercontent.com/62925639/95828709-492b3a00-0d70-11eb-91b8-326ca77f94f9.jpg" alt="forget_email2" width="15%" height="15%" />

<br>*Phone Auth*    
<img src="https://user-images.githubusercontent.com/62925639/95828731-4c262a80-0d70-11eb-96a5-302fac11a185.jpg" alt="phoneAuth" width="15%" height="15%" /> <img src="https://user-images.githubusercontent.com/62925639/95828737-4cbec100-0d70-11eb-976d-c9a9d6c3b268.jpg" alt="phoneAuth2" width="15%" height="15%" /> <img src="https://user-images.githubusercontent.com/62925639/95828733-4c262a80-0d70-11eb-9df4-7778c02160bd.jpg" alt="phoneAuth_success" width="15%" height="15%" />

<br>*Verify Address*    
<img src="https://user-images.githubusercontent.com/62925639/95828720-4a5c6700-0d70-11eb-8de0-27d448f96a0a.jpg" alt="map1" width="15%" height="15%" /> <img src="https://user-images.githubusercontent.com/62925639/95828722-4a5c6700-0d70-11eb-9047-6169e240019b.jpg" alt="map2" width="15%" height="15%" /> <img src="https://user-images.githubusercontent.com/62925639/95828726-4af4fd80-0d70-11eb-8a70-556ad4d2f843.jpg" alt="map_success" width="15%" height="15%" />

<br>*Main view*    
<img src="https://user-images.githubusercontent.com/62925639/95828717-49c3d080-0d70-11eb-8ce1-9a3202f521ed.jpg" alt="main" width="15%" height="15%" />

<br>*Signle view for one post*      
<img src="https://user-images.githubusercontent.com/62925639/95828691-46304980-0d70-11eb-9acc-b7e384b78c03.jpg" alt="post" width="15%" height="15%" /> <img src="https://user-images.githubusercontent.com/62925639/95828696-46c8e000-0d70-11eb-8c64-c11d7fe792c1.jpg" alt="post2" width="15%" height="15%" /> <img src="https://user-images.githubusercontent.com/62925639/95828705-47fa0d00-0d70-11eb-8c6e-25c83a349c37.jpg" alt="post_del" width="15%" height="15%" />

<br>*Posting format*        
<img src="https://user-images.githubusercontent.com/62925639/95828742-4e888480-0d70-11eb-8c39-ade616f2bd70.jpg" alt="write2" width="15%" height="15%" /> <img src="https://user-images.githubusercontent.com/62925639/95828741-4defee00-0d70-11eb-8a79-9c079b8ee95e.jpg" alt="write" width="15%" height="15%" /> <img src="https://user-images.githubusercontent.com/62925639/95828744-4e888480-0d70-11eb-956f-784d744cb16c.jpg" alt="write_error" width="15%" height="15%" /> <img src="https://user-images.githubusercontent.com/62925639/95828745-4f211b00-0d70-11eb-96ff-cac74e124caf.jpg" alt="write_exit" width="15%" height="15%" />

<br>*List of chat rooms*       
<img src="https://user-images.githubusercontent.com/62925639/95828699-46c8e000-0d70-11eb-9356-7364045a0e6e.jpg" alt="chatlist" width="15%" height="15%" /> 

<br>*A chat room*    
<img src="https://user-images.githubusercontent.com/62925639/95828702-47617680-0d70-11eb-947e-c81880505a16.jpg" alt="chatting1" width="15%" height="15%" /> <img src="https://user-images.githubusercontent.com/62925639/95828703-47617680-0d70-11eb-93bb-ab12d730f2dd.jpg" alt="chatting2" width="15%" height="15%" /> 

<br>*Settings*   
<img src="https://user-images.githubusercontent.com/62925639/95828728-4b8d9400-0d70-11eb-9bfa-e994eaf5144e.jpg" alt="mypage1" width="15%" height="15%" /> <img src="https://user-images.githubusercontent.com/62925639/95828714-49c3d080-0d70-11eb-952c-029e08a3d2e0.jpg" alt="logout" width="15%" height="15%" /> <img src="https://user-images.githubusercontent.com/62925639/95828730-4b8d9400-0d70-11eb-809c-982179a32300.jpg" alt="mypage_edit" width="15%" height="15%" />
