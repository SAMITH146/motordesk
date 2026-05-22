<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/variables.css?v=1.2" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css?v=1.2" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/navbar.css?v=1.2" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-panel.css?v=1.2" />
    <title>Historial de Compras | MotorDesk</title>
</head>

<body>
    <header class="navbar">
        <div class="navbar__logo">
            <img src="${pageContext.request.contextPath}/LogoI_mg/Logo_blanco.png" alt="Logo MotorDesk"
                class="navbar__logo-img" />
        </div>

        <nav class="navbar__menu" aria-label="Menu principal">
            <a href="${pageContext.request.contextPath}/AdminDashboard" class="navbar__menu-item">Dashboard</a>
            <a href="${pageContext.request.contextPath}/MecanicoController" class="navbar__menu-item">Mecánicos</a>
            <a href="${pageContext.request.contextPath}/ProductoController" class="navbar__menu-item">Productos</a>
            <a href="${pageContext.request.contextPath}/admin/ingreso" class="navbar__menu-item">Ingresar Pedido</a>
            <a href="${pageContext.request.contextPath}/admin/historialCompras" class="navbar__menu-item active">Historial Compras</a>
            <a href="${pageContext.request.contextPath}/OrdenController?action=listAll" class="navbar__menu-item">Órdenes</a>
        </nav>

        <div class="navbar__session">
            <div class="navbar__user-info">
                <span class="navbar__user-name">${sessionScope.usuarioLogueado.nombre}</span>
                <span class="navbar__user-role">${sessionScope.usuarioLogueado.idCargo}</span>
            </div>
            <a href="#logoutModal" class="navbar__session-btn">
                <img src="${pageContext.request.contextPath}/LogoI_mg/cerrarseccion_blanco.png"
                    alt="Cerrar sesión" class="navbar__session-icon" />
            </a>
        </div>
    </header>

    <main class="admin-main fade-in">
        <section class="admin-section">
            <header style="margin-bottom: 2rem;">
                <h1 class="admin-section__title">Historial de Ingresos / Compras</h1>
                <p class="admin-section__subtitle">Registro de todos los pedidos ingresados con proveedor y fecha.</p>
            </header>

            <article class="admin-card" style="padding: 1.5rem;">
                <div style="overflow-x: auto;">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>N° Orden</th>
                                <th>Proveedor</th>
                                <th>Fecha de Compra</th>
                                <th>Total Pagado</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty requestScope.compras}">
                                    <c:forEach var="compra" items="${requestScope.compras}">
                                        <tr>
                                            <td># ${compra.idCompra}</td>
                                            <td>${not empty compra.nombreProveedor ? compra.nombreProveedor : 'Proveedor Eliminado'}</td>
                                            <td><fmt:formatDate value="${compra.fechaCompra}" pattern="dd/MM/yyyy" /></td>
                                            <td style="font-weight: bold; color: #2ecc71;">$ <fmt:formatNumber value="${compra.total}" pattern="#,##0.00" /></td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="4" style="text-align: center; opacity: 0.6; padding: 20px;">No hay registros de compras.</td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </article>
        </section>
    </main>

    <!-- Log out Modal (CSS Only) -->
    <div id="logoutModal" class="modal-css">
        <div class="modal-content-css">
            <h2>¿Cerrar Sesión?</h2>
            <p>Estás a punto de salir del sistema.<br>¿Estás seguro?</p>
            <div class="modal-buttons-css">
                <a href="#" class="btn-modal-css btn-modal-css--cancel">No, quedarme</a>
                <a href="${pageContext.request.contextPath}/LogoutController" class="btn-modal-css btn-modal-css--confirm">Sí, salir</a>
            </div>
        </div>
    </div>
</body>
</html>
