<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <title>Iniciar Sesión | MotorDesk</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/Loguin.css">

    </head>

    <body>
        <main class="container-login">
            <article class="login">

                <header class="login__header">
                    <img class="login__logo"
                         src="${pageContext.request.contextPath}/LogoI_mg/LogoMotorDesk.png">
                    <h1 class="login__titulo">Iniciar Sesión</h1>
                </header>

                <c:if test="${not empty mensajeError}">
                    <div style="color:red; text-align:center;">
                        ${mensajeError}
                    </div>
                </c:if>

                <form class="login__form"
                      action="${pageContext.request.contextPath}/LoginController"
                      method="post">

                    <div class="login__grupo">
                        <label>Ingrese su PIN</label>
                        <input type="password" name="pin" maxlength="10" required>
                    </div>

                    <button type="submit">Iniciar Sesión</button>
                </form>

            </article>
        </main>
    </body>
</html>