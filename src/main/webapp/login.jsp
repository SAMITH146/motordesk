<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8">
            <title>Iniciar Sesión | MotorDesk</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css?v=2">
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/Loguin.css?v=2">
        </head>

        <body>
            <main class="container-login">
                <article class="login">

                    <header class="login__header">
                        <img class="login__logo" src="${pageContext.request.contextPath}/LogoI_mg/Logo_blanco.png"
                            alt="Logo de MotorDesk">
                        <h1 class="login__titulo">Bienvenido</h1>
                        <p class="login__subtitulo">Ingresa a tu cuenta de MotorDesk</p>
                    </header>

                    <c:if test="${not empty mensajeError}">
                        <div class="login__error">
                            <span>⚠️ ${mensajeError}</span>
                        </div>
                    </c:if>

                    <form class="login__form" action="${pageContext.request.contextPath}/LoginController" method="post">

                        <div class="login__grupo">
                            <label class="login__label">PIN de Acceso</label>
                            <input class="login__input" type="password" name="pin" maxlength="10" placeholder="••••••••"
                                required pattern="\d+" title="Ingrese solo números" oninput="this.value = this.value.replace(/[^0-9]/g, '')">
                        </div>

                        <button class="login__boton" type="submit">Iniciar Sesión</button>
                    </form>
                </article>
            </main>
        </body>

        </html>