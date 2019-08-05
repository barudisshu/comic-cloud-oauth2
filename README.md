Akka based oauth2 
=================



仅token和code才需要sharding region，其它领域实体不需要

## 创建用户

```
put localhost:9000/api/account
{
	"username": "galudisu",
	"password": "$ea(oo2!f",
	"salt": "ejOpaakl108",
	"email": "galudisu@gmail.com"
}
```

## 申请客户端

1. 申请者Id，必须在account中有记录
2. 申请者Id，需要是开发者角色(此处不验证)
3. 申请者Id，拥有相关权限资格(此处不验证)
4. 申请的网站回调地址

授权服务平台，不作表单验证逻辑

```
put localhost:9000/api/client
{
    "account_id": "b19b28ae-6af5-4603-b588-02be213e2262",
    "redirect_uri": "http://localhost:3000/callback"
}
```

## 申请Code

1. 申请者Id，必须在account中有记录
2. 客户端Id，必须在client中有记录
3. 申请者网站回调地址

```
put localhost:9000/api/code
{
    "account_id": "b19b28ae-6af5-4603-b588-02be213e2262",
    "client_id": "8a70c2923877f4caf6ab45538457c5d628e6bce0"
}
```

## 获取Code信息

1. 每读取一次code，这个实体应该变为passivation
2. 读取的code信息，会作为协作域，用于生成有效的token

首次获取生效，再次获取变更为404。

```
get localhost:9000/api/code/6a734
```

## 产生token

1. 由appid和appkey产生，客户端模式，不包含重定向地址

```
post localhost:9000/api/access_token
{
    "appid": "8a70c2923877f4caf6ab45538457c5d628e6bce0",
    "appkey": "3aa585d12085692348199b5227727bbc2c42a395",
    "grant_type": "client_credentials"
}
```

2. 第三方平台模式，必须包含重定向地址，以及第三方申请的一次性code

```
post localhost:9000/api/access_token
{
    "appid": "8a70c2923877f4caf6ab45538457c5d628e6bce0",
    "appkey": "3aa585d12085692348199b5227727bbc2c42a395",
    "redirect_uri": "http://baidu.com",
    "code": "9afle",
    "grant_type": "authorization_code"
}
```

3. 账号密码模式，或者叫表单模式

```
post localhost:9000/api/access_token
{
    "appid": "8a70c2923877f4caf6ab45538457c5d628e6bce0",
    "appkey": "3aa585d12085692348199b5227727bbc2c42a395",
    "username": "galudisu",
    "password": "XFFkla!2",
    "grant_type": "password"
}
```

4. Refresh token

```
post localhost:9000/api/access_token
{
    "appid": "8a70c2923877f4caf6ab45538457c5d628e6bce0",
    "appkey": "3aa585d12085692348199b5227727bbc2c42a395",
    "refreshToken": "ejpxxklakfalkd.......",
    "grant_type": "refresh_token",
}
```

## 统一资源入口

```bash
curl --dump-header -H "Authorization: Bearer ${access_token}" http://localhost:9000/api/resources
```