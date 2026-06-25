<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/variables.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/navbar.css?v=1.2" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-panel.css" />
    <title>Historial de Facturas | MotorDesk</title>
    <style>
        .filter-container {
            display: flex;
            gap: 15px;
            margin-bottom: 20px;
            flex-wrap: wrap;
        }
        .filter-input {
            padding: 10px 15px;
            border-radius: 8px;
            border: 1px solid rgba(255, 255, 255, 0.1);
            background: rgba(0, 0, 0, 0.2);
            color: #fff;
            font-size: 0.9rem;
            width: 250px;
        }
        .filter-input::placeholder {
            color: #94a3b8;
        }
        .badge {
            padding: 4px 8px;
            border-radius: 6px;
            font-size: 0.75rem;
            font-weight: 600;
        }
        .badge-efectivo { background: rgba(16, 185, 129, 0.2); border: 1px solid #10b981; color: #10b981; }
        .badge-tarjeta { background: rgba(59, 130, 246, 0.2); border: 1px solid #3b82f6; color: #3b82f6; }
        .badge-transferencia { background: rgba(168, 85, 247, 0.2); border: 1px solid #a855f7; color: #a855f7; }
    </style>
</head>
<body>
    <header class="navbar">
        <div class="navbar__logo">
            <img src="${pageContext.request.contextPath}/LogoI_mg/Logo_blanco.png" alt="Logo MotorDesk" class="navbar__logo-img" />
        </div>
        <nav class="navbar__menu" aria-label="Menu principal">
            <a href="${pageContext.request.contextPath}/AdminDashboard" class="navbar__menu-item">Dashboard</a>
            <a href="${pageContext.request.contextPath}/MecanicoController" class="navbar__menu-item">Mecánicos</a>
            <a href="${pageContext.request.contextPath}/ClienteController" class="navbar__menu-item">Clientes</a>
            <a href="${pageContext.request.contextPath}/ProductoController" class="navbar__menu-item">Productos</a>
            <a href="${pageContext.request.contextPath}/admin/ingreso" class="navbar__menu-item">Ingresar Pedido</a>
            <a href="${pageContext.request.contextPath}/admin/historialCompras" class="navbar__menu-item">Historial Compras</a>
            <a href="${pageContext.request.contextPath}/OrdenController?action=listAll" class="navbar__menu-item">Órdenes</a>

            <a href="${pageContext.request.contextPath}/BitacoraController" class="navbar__menu-item">Auditoría</a>
        </nav>
        <div class="navbar__session">
            <div class="navbar__user-info">
                <span class="navbar__user-name">${sessionScope.usuarioLogueado.nombre}</span>
            </div>
            <a href="#logoutModal" class="navbar__session-btn">
                <img src="${pageContext.request.contextPath}/LogoI_mg/cerrarseccion_blanco.png" alt="Cerrar sesión" class="navbar__session-icon" />
            </a>
        </div>
    </header>

    <main class="admin-main fade-in">
        <header class="admin-section__header">
            <h1 class="admin-section__title">Historial de Facturas Comerciales</h1>
            <p class="admin-section__subtitle">Registro contable y comercial de cobros realizados por servicios.</p>
        </header>

        <div class="filter-container">
            <input type="text" id="searchInput" class="filter-input" placeholder="Buscar por número o ID de orden..." onkeyup="filtrarTabla()" />
            <select id="paymentFilter" class="filter-input" onchange="filtrarTabla()" style="width: 200px;">
                <option value="">Todos los métodos</option>
                <option value="EFECTIVO">Efectivo</option>
                <option value="TARJETA">Tarjeta</option>
                <option value="TRANSFERENCIA">Transferencia</option>
            </select>
        </div>

        <section class="admin-card">
            <div style="overflow-x: auto;">
                <!-- Tabla principal que lista las facturas comerciales emitidas y sus metadatos -->
                <table class="admin-table" id="facturasTable">
                    <thead>
                        <tr>
                            <th style="width: 150px;">Factura N°</th>
                            <th style="width: 100px;">Orden ID</th>
                            <th style="width: 200px;">Fecha Emisión</th>
                            <th style="text-align: right; width: 150px;">Subtotal</th>
                            <th style="text-align: right; width: 150px;">IVA (19%)</th>
                            <th style="text-align: right; width: 150px;">Total</th>
                            <th style="text-align: center; width: 150px;">Método de Pago</th>
                            <th style="text-align: center; width: 120px;">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <%-- Bucle que recorre la listaFacturas y muestra los datos clave como subtotal, total e impuestos --%>
                            <c:when test="${not empty listaFacturas}">
                                <c:forEach var="fac" items="${listaFacturas}">
                                    <tr>
                                        <td><strong>${fac.numeroFactura}</strong></td>
                                        <td>#${fac.idOrdenFk}</td>
                                        <td style="color: #94a3b8; font-size: 0.85rem;">
                                            <fmt:formatDate value="${fac.fechaEmision}" pattern="dd/MM/yyyy hh:mm a" />
                                        </td>
                                        <td style="text-align: right; font-weight: 600;">
                                            <fmt:formatNumber value="${fac.subtotal}" type="currency" currencySymbol="$" />
                                        </td>
                                        <td style="text-align: right; color: #94a3b8;">
                                            <fmt:formatNumber value="${fac.iva}" type="currency" currencySymbol="$" />
                                        </td>
                                        <td style="text-align: right; font-weight: 700; color: #10b981;">
                                            <fmt:formatNumber value="${fac.total}" type="currency" currencySymbol="$" />
                                        </td>
                                        <td style="text-align: center;">
                                            <c:set var="pmUpper" value="${fac.metodoPago.toUpperCase()}" />
                                            <c:choose>
                                                <c:when test="${pmUpper == 'EFECTIVO'}">
                                                    <span class="badge badge-efectivo">💵 Efectivo</span>
                                                </c:when>
                                                <c:when test="${pmUpper == 'TARJETA'}">
                                                    <span class="badge badge-tarjeta">💳 Tarjeta</span>
                                                </c:when>
                                                <c:when test="${pmUpper == 'TRANSFERENCIA'}">
                                                    <span class="badge badge-transferencia">📱 Transferencia</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge" style="background: rgba(255, 255, 255, 0.1); color: #fff;">${fac.metodoPago}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <a href="${pageContext.request.contextPath}/OrdenController?action=verFactura&id_orden=${fac.idOrdenFk}" 
                                               class="admin-btn" style="padding: 4px 10px; font-size: 0.8rem; background: rgba(59, 130, 246, 0.15); border: 1px solid rgba(59, 130, 246, 0.3); color: #3b82f6; box-shadow: none;">
                                                🔍 Ver Detalles
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="8" style="text-align: center; font-style: italic; opacity: 0.6; padding: 2rem;">
                                        No se han emitido facturas todavía.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </section>
    </main>

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

    <script>
        function filtrarTabla() {
            var searchVal = document.getElementById("searchInput").value.toLowerCase();
            var payVal = document.getElementById("paymentFilter").value.toUpperCase();
            var table = document.getElementById("facturasTable");
            var rows = table.getElementsByTagName("tbody")[0].getElementsByTagName("tr");

            for (var i = 0; i < rows.length; i++) {
                var row = rows[i];
                if (row.getElementsByTagName("td").length < 8) continue;

                var num = row.getElementsByTagName("td")[0].textContent.toLowerCase();
                var order = row.getElementsByTagName("td")[1].textContent.toLowerCase();
                var method = row.getElementsByTagName("td")[6].textContent.toUpperCase();

                var matchesSearch = num.includes(searchVal) || order.includes(searchVal);
                var matchesPay = payVal === "" || method.includes(payVal);

                if (matchesSearch && matchesPay) {
                    row.style.display = "";
                } else {
                    row.style.display = "none";
                }
            }
        }
    </script>
</body>
</html>
