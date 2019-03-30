# 基于Akka统一认证授权服务

## 创建用户

```
post localhost:9000/api/account
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
post localhost:9000/api/client
{
    "account_id": "galudisu",
    "redirect_uri": "http://localhost:3000/callback"
}
```
