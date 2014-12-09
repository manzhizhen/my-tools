<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>sypay-openapi</title>
<!-- <script type="text/javascript" src="js/jquery-1.11.1.js"></script> -->

</head>

<body>

	<div class="container">
		<h1>sypay-openapi</h1>

		<c:if test="${not empty param.authentication_error}">
			<h1>靠!</h1>
			<p class="error">登录失败.</p>
		</c:if>
		<c:if test="${not empty param.authorization_error}">
			<h1>靠!</h1>
			<p class="error">你没有权限读取该资源.</p>
		</c:if>

		<div>
			<form action="<c:url value="/login/submit"/>" method="post">
				<div>
					<label for="username">用户名:</label> <input id="username"
						class="form-control" type='text' name='j_username'
						value="zhizhen" />
				</div>
				<div>
					<label for="password">密码:</label> <input id="password"
						class="form-control" type='text' name='j_password' value="zhizhen" />
				</div>
				<button type="submit">登 录</button>
				<input type="hidden" name="${_csrf.parameterName}"
					value="${_csrf.token}" />
			</form>

		</div>
	</div>
</body>
</html>
