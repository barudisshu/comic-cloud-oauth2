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

授权服务平台，不作表单验证逻辑

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

## 获取Code信息

1. 每读取一次code，这个实体应该变为passivation
2. 读取的code信息，会作为协作域，用于生成有效的token

首次获取生效，再次获取变更为404。

```
get localhost:9000/api/code/6a734
```

## 产生token