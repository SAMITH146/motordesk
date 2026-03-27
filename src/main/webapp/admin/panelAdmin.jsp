<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-dashboard.css" />
            <title>Dashboard Administrador | MotorDesk</title>
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
                    <a href="${pageContext.request.contextPath}/MecanicoController"
                        class="navbar__menu-item">Mecánicos</a>
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

            <main class="admin-main fade-in">
                <section class="admin-section">
                    <header>
                        <h1 class="admin-section__title">Bienvenido, ${sessionScope.usuarioLogueado.nombre}</h1>
                        <p class="admin-section__subtitle">Aquí tienes un resumen rápido de MotorDesk hoy.</p>
                    </header>

                    <div class="admin-dashboard__grid">
                        <article class="admin-card">
                            <div class="admin-card__header">
                                <h3 class="admin-card__title">Mecánicos Activos</h3>
                                <div class="admin-card__icon">🧑‍🔧</div>
                            </div>
                            <div class="admin-card__value">
                                <c:out
                                    value="${not empty requestScope.totalMecanicos ? requestScope.totalMecanicos : '0'}" />
                            </div>
                            <a href="${pageContext.request.contextPath}/MecanicoController"
                                class="admin-card__link">Gestionar Mecánicos →</a>
                        </article>

                        <article class="admin-card">
                            <div class="admin-card__header">
                                <h3 class="admin-card__title">Productos en Inventario</h3>
                                <div class="admin-card__icon">📦</div>
                            </div>
                            <div class="admin-card__value">
                                <c:out
                                    value="${not empty requestScope.totalProductos ? requestScope.totalProductos : '0'}" />
                            </div>
                            <a href="${pageContext.request.contextPath}/pages/admin_productos.jsp"
                                class="admin-card__link">Ver Inventario completo →</a>
                        </article>

                        <article class="admin-card">
                            <div class="admin-card__header">
                                <h3 class="admin-card__title">Órdenes Abiertas</h3>
                                <div class="admin-card__icon">📋</div>
                            </div>
                            <div class="admin-card__value">
                                <c:out
                                    value="${not empty requestScope.totalOrdenes ? requestScope.totalOrdenes : '0'}" />
                            </div>
                            <a href="${pageContext.request.contextPath}/pages/admin_ordenes.jsp"
                                class="admin-card__link">Revisar Órdenes →</a>
                        </article>

                        <article class="admin-card card-warning">
                            <div class="admin-card__header">
                                <h3 class="admin-card__title">Stock Bajo/Crítico</h3>
                                <div class="admin-card__icon icon-warning">⚠️</div>
                            </div>
                            <div class="admin-card__value text-warning">
                                <c:out
                                    value="${not empty requestScope.stockCritico ? requestScope.stockCritico : '0'}" />
                            </div>
                            <a href="${pageContext.request.contextPath}/pages/admin_productos.jsp"
                                class="admin-card__link link-warning">Ir al resúmen detallado →</a>
                        </article>
                    </div>
                </section>

                <footer class="admin-footer">
                    <p>&copy; 2024 MotorDesk Web System. Todos los derechos reservados.</p>
                </footer>
            </main>
        </body>

        </html>