# 基于Akka统一认证授权服务

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

```
put localhost:9000/api/client
{
    "accountId": "b19b28ae-6af5-4603-b588-02be213e2262",
    "redirectUri": "http://localhost:3000/callback"
}
```

## 申请Code

1. 申请者Id，必须在account中有记录
2. 客户端Id，必须在client中有记录
3. 申请者网站回调地址

```
put localhost:9000/api/code
{
    "accountId": "b19b28ae-6af5-4603-b588-02be213e2262",
    "clientId": "8a70c2923877f4caf6ab45538457c5d628e6bce0"
}
```

## 由Code获取token身份

```
get localhost:9000/api/code?codeId=6a734
```
