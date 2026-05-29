 +<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/variables.css" />
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css" />
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/navbar.css" />
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-panel.css" />
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/panel.css" />
            <title>Panel del Mecánico | MotorDesk</title>
        </head>

        <body>
            <header class="navbar">
                <div class="navbar__logo">
                    <img src="${pageContext.request.contextPath}/LogoI_mg/Logo_blanco.png" alt="Logo MotorDesk"
                        class="navbar__logo-img" />
                </div>

                <nav class="navbar__menu" aria-label="Menu principal">
                    <a href="${pageContext.request.contextPath}/pages/admin.jsp"
                        class="navbar__menu-item active">Dashboard</a>
                    <a href="${pageContext.request.contextPath}/pages/admin_productos.jsp"
                        class="navbar__menu-item">Productos</a>
                    <a href="${pageContext.request.contextPath}/pages/admin_ordenes.jsp"
                        class="navbar__menu-item">Órdenes</a>
                </nav>

                <div class="navbar__session">
                    <div class="navbar__user-info">
                        <span class="navbar__user-name">${sessionScope.usuarioLogueado.nombre}</span>
                        <span class="navbar__user-role">${sessionScope.usuarioLogueado.idCargo}</span>
                    </div>
                    <a href="${pageContext.request.contextPath}/LogoutController" class="navbar__session-btn">
                        <img src="${pageContext.request.contextPath}/LogoI_mg/cerrarseccion_blanco.png"
                            alt="Cerrar sesión" class="navbar__session-icon" />
                    </a>
                </div>
            </header>

            <!-- MAIN: Contenido principal del panel -->
            <main class="panel admin-main">

                <!-- Sección de gráfica -->
                <section class="container-grafica">
                    <h2 class="visually-hidden">Estadisticas semanales</h2>

                    <div class="grafica">
                        <!-- Estos valores podrían cargarse dinámicamente también con JSTL mapeando alturas -->
                        <div class="barra" id="b1"
                            style="height: ${requestScope.alturaB1 != null ? requestScope.alturaB1 : '0'}%;">
                            <span>Lun</span>
                        </div>
                        <div class="barra" id="b2"
                            style="height: ${requestScope.alturaB2 != null ? requestScope.alturaB2 : '0'}%;">
                            <span>Mar</span>
                        </div>
                        <div class="barra" id="b3"
                            style="height: ${requestScope.alturaB3 != null ? requestScope.alturaB3 : '0'}%;">
                            <span>Mié</span>
                        </div>
                        <div class="barra" id="b4"
                            style="height: ${requestScope.alturaB4 != null ? requestScope.alturaB4 : '0'}%;">
                            <span>Jue</span>
                        </div>
                        <div class="barra" id="b5"
                            style="height: ${requestScope.alturaB5 != null ? requestScope.alturaB5 : '0'}%;">
                            <span>Vie</span>
                        </div>
                        <div class="barra" id="b6"
                            style="height: ${requestScope.alturaB6 != null ? requestScope.alturaB6 : '0'}%;">
                            <span>Sáb</span>
                        </div>
                    </div>
                    <!-- Resumen semanal -->
                    <aside class="resumen">
                        <div>Servicios <span>${requestScope.serviciosTotales != null ? requestScope.serviciosTotales
                                : '0'}</span></div>
                        <div>Órdenes <span>${requestScope.ordenesTotales != null ? requestScope.ordenesTotales :
                                '0'}</span></div>
                        <div>Total <span>$
                                <c:out value="${requestScope.dineroTotal != null ? requestScope.dineroTotal : '0'}" />
                            </span></div>
                    </aside>
                </section>

                <!-- Sección de Inventario - Stock Bajo (Podemos usar foreach) -->
                <section class="container-inventario">
                    <h2 class="inventario-titulo">⚠️ Productos con Stock Bajo</h2>
                    <p class="inventario-subtitulo">Productos que necesitan reposición pronto</p>

                    <div class="inventario-grid">

                        <c:choose>
                            <c:when test="${not empty requestScope.productosStockBajo}">
                                <c:forEach var="prod" items="${requestScope.productosStockBajo}">
                                    <article class="producto-card ${prod.stock <= 5 ? 'critico' : 'bajo'}">
                                        <div class="producto-header">
                                            <h3 class="producto-nombre">
                                                <c:out value="${prod.nombre}" />
                                            </h3>
                                            <span class="producto-categoria">
                                                <c:out value="${prod.categoria}" />
                                            </span>
                                        </div>
                                        <div class="producto-info">
                                            <div class="stock-cantidad">
                                                <span class="stock-numero">
                                                    <c:out value="${prod.stock}" />
                                                </span>
                                                <span class="stock-texto">unidades</span>
                                            </div>
                                            <div
                                                class="stock-estado ${prod.stock <= 5 ? 'critico-badge' : 'bajo-badge'}">
                                                ${prod.stock <= 5 ? 'CRÍTICO' : 'BAJO' } </div>
                                            </div>
                                            <div class="stock-barra">
                                                <div class="stock-nivel" style="width: ${prod.stock * 10}%;"></div>
                                            </div>
                                    </article>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div class="mensaje-exito"
                                    style="grid-column: 1 / -1; text-align: center; padding: 20px; background: rgba(0, 255, 0, 0.1); border-radius: 8px;">
                                    <h3 style="color: #2e7d32; margin-bottom: 5px;">¡Todo en orden!</h3>
                                    <p style="color: #4caf50;">No hay productos con stock crítico en este momento en la
                                        Base de Datos.</p>
                                </div>
                            </c:otherwise>
                        </c:choose>

                    </div>
                </section>

            </main>
        </body>

        </html>