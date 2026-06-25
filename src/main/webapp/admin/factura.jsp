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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/factura.css?v=1.0" />
    <title>Factura Comercial | MotorDesk</title>
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
            <a href="${pageContext.request.contextPath}/ProveedorController" class="navbar__menu-item">Proveedores</a>
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
            <h1 class="admin-section__title">Factura de Servicio</h1>
            <p class="admin-section__subtitle">Comprobante comercial emitido por orden de servicio.</p>
        </header>

        <section class="admin-card factura-card">
            <div class="factura-watermark">${orden.estado}</div>

            <!-- CABECERA -->
            <div class="factura-header">
                <div class="factura-header__taller">
                    <div class="factura-header__logo">
                        <img src="${pageContext.request.contextPath}/LogoI_mg/Logo_blanco.png" alt="Logo" class="factura-header__logo-img" />
                        <span class="factura-header__logo-text">MotorDesk</span>
                    </div>
                    <div class="factura-header__info">
                        <strong>MotorDesk S.A.S.</strong><br/>
                        NIT: 900.123.456-7<br/>
                        Dirección: Av. Principal Calle 45 #12-34<br/>
                        Teléfono: 300 987 6543 | info@motordesk.com<br/>
                        <em>Bucaramanga, Santander, Colombia</em>
                    </div>
                </div>

                <div class="factura-header__meta">
                    <span class="factura-header__meta-title">FACTURA COMERCIAL</span>
                    <span class="factura-header__meta-id">
                        <c:choose>
                            <c:when test="${not empty facturaRegistrada}">
                                ${facturaRegistrada.numeroFactura}
                            </c:when>
                            <c:otherwise>
                                N° MD-<fmt:formatNumber value="${orden.idOrden}" pattern="0000"/>
                            </c:otherwise>
                        </c:choose>
                    </span>
                    <span class="factura-header__meta-text">
                        <strong>Fecha Emisión:</strong> 
                        <c:choose>
                            <c:when test="${not empty facturaRegistrada}">
                                <fmt:formatDate value="${facturaRegistrada.fechaEmision}" pattern="dd/MM/yyyy hh:mm a" />
                            </c:when>
                            <c:otherwise>
                                <fmt:formatDate value="${orden.fecha}" pattern="dd/MM/yyyy" />
                            </c:otherwise>
                        </c:choose>
                    </span>
                    <c:if test="${not empty facturaRegistrada}">
                        <span class="factura-header__meta-text">
                            <strong>Método Pago:</strong> ${facturaRegistrada.metodoPago}
                        </span>
                    </c:if>
                    <div>
                        <span class="factura-status factura-status--${orden.estado.toLowerCase()}">${orden.estado}</span>
                    </div>
                </div>
            </div>

            <!-- BLOQUES CLIENTE / VEHÍCULO -->
            <div class="factura-details-grid">
                <!-- CLIENTE -->
                <div class="factura-section-block">
                    <h3 class="factura-section-block__title">👤 Datos del Cliente</h3>
                    <div class="factura-section-block__item">
                        <span class="factura-section-block__label">Nombre:</span>
                        <span class="factura-section-block__val">${not empty cliente ? cliente.nombre : 'No especificado'}</span>
                    </div>
                    <div class="factura-section-block__item">
                        <span class="factura-section-block__label">Cédula/NIT:</span>
                        <span class="factura-section-block__val">${not empty cliente ? cliente.documento : 'No especificado'}</span>
                    </div>
                    <div class="factura-section-block__item">
                        <span class="factura-section-block__label">Dirección:</span>
                        <span class="factura-section-block__val">${not empty cliente ? cliente.direccion : 'No especificado'}</span>
                    </div>
                </div>

                <!-- VEHÍCULO -->
                <div class="factura-section-block">
                    <h3 class="factura-section-block__title">🚗 Datos del Vehículo</h3>
                    <div class="factura-section-block__item">
                        <span class="factura-section-block__label">Placa:</span>
                        <span class="factura-section-block__val" style="color: var(--accent-light); letter-spacing: 1px;">${not empty vehiculo ? vehiculo.placa : orden.placaVehiculo}</span>
                    </div>
                    <div class="factura-section-block__item">
                        <span class="factura-section-block__label">Marca:</span>
                        <span class="factura-section-block__val">${not empty vehiculo ? vehiculo.marca : 'N/A'}</span>
                    </div>
                    <div class="factura-section-block__item">
                        <span class="factura-section-block__label">Modelo:</span>
                        <span class="factura-section-block__val">${not empty vehiculo ? vehiculo.modelo : 'N/A'} (Año: ${not empty vehiculo ? vehiculo.anio : 'N/A'})</span>
                    </div>
                </div>
            </div>

            <!-- DETALLE DE ÍTEMS -->
            <div class="factura-section-block" style="padding: 1rem 0 0 0; overflow-x: auto;">
                <h3 class="factura-section-block__title" style="padding: 0 1.5rem 0.5rem 1.5rem; margin-bottom: 0;">🔧 Detalle de Servicios y Repuestos</h3>
                <table class="factura-items-table">
                    <thead>
                        <tr>
                            <th>Tipo</th>
                            <th>Descripción</th>
                            <th style="text-align: center;">Cantidad</th>
                            <th style="text-align: right;">Precio Unitario</th>
                            <th style="text-align: right;">Subtotal</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%-- ===== SERVICIOS DE MANO DE OBRA ===== --%>
                        <c:choose>
                            <%-- Bucle que recorre la lista de servicios (mano de obra) realizados en la orden y los muestra en la factura --%>
                            <c:when test="${not empty requestScope.servicios}">
                                <c:forEach var="srv" items="${requestScope.servicios}">
                                    <tr>
                                        <td style="opacity:0.5; font-size:0.8rem;">SERVICIO</td>
                                        <td>
                                            <span style="display:inline-block; background:rgba(16,185,129,0.1); border:1px solid rgba(16,185,129,0.2); color:#10b981; padding:2px 8px; border-radius:20px; font-size:0.75rem; margin-right:6px;">Mano de Obra</span>
                                            ${srv.nombre}
                                        </td>
                                        <td style="text-align: center;">1</td>
                                        <td style="text-align: right;"><fmt:formatNumber value="${srv.valorCobrado}" type="currency" currencySymbol="$" /></td>
                                        <td style="text-align: right; font-weight: 700;"><fmt:formatNumber value="${srv.valorCobrado}" type="currency" currencySymbol="$" /></td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="5" style="text-align:center; opacity:0.4; padding:1rem; font-size:0.85rem; font-style:italic;">Sin servicios de mano de obra registrados.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>

                        <%-- ===== SEPARADOR REPUESTOS ===== --%>
                        <c:if test="${not empty requestScope.detalles}">
                            <tr>
                                <td colspan="5" style="padding:4px 15px; background:rgba(255,255,255,0.02); border-bottom:1px dashed rgba(255,255,255,0.06);">
                                    <span style="font-size:0.75rem; color:#475569; text-transform:uppercase; letter-spacing:1px;">Repuestos y Materiales</span>
                                </td>
                            </tr>
                        </c:if>

                        <%-- ===== REPUESTOS ===== --%>
                        <c:choose>
                            <%-- Bucle que recorre los repuestos/materiales (detalles) consumidos en la orden para cobrarlos --%>
                            <c:when test="${not empty requestScope.detalles}">
                                <c:forEach var="d" items="${requestScope.detalles}">
                                    <tr>
                                        <td style="opacity:0.5; font-size:0.8rem;">#${d.idProductoFk}</td>
                                        <td>${d.nombreProducto}</td>
                                        <td style="text-align: center;">${d.cantidad}</td>
                                        <td style="text-align: right;"><fmt:formatNumber value="${d.subtotal / d.cantidad}" type="currency" currencySymbol="$" /></td>
                                        <td style="text-align: right; font-weight: 700;"><fmt:formatNumber value="${d.subtotal}" type="currency" currencySymbol="$" /></td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="5" style="text-align:center; opacity:0.5; padding:1rem; font-style:italic; font-size:0.85rem;">No se registraron repuestos en esta orden.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <!-- TOTALES -->
            <div class="factura-totals-container">
                <div class="factura-totals-box">
                    <div class="factura-total-row">
                        <span style="color: #94a3b8;">Subtotal:</span>
                        <span><fmt:formatNumber value="${orden.total * 0.81}" type="currency" currencySymbol="$" /></span>
                    </div>
                    <div class="factura-total-row">
                        <span style="color: #94a3b8;">IVA (19% Incluido):</span>
                        <span><fmt:formatNumber value="${orden.total * 0.19}" type="currency" currencySymbol="$" /></span>
                    </div>
                    <div class="factura-total-row factura-total-row--grand">
                        <span>Total a Pagar:</span>
                        <span><fmt:formatNumber value="${orden.total}" type="currency" currencySymbol="$" /></span>
                    </div>
                </div>
            </div>

            <!-- PIE / ACCIONES -->
            <footer class="factura-actions">
                <div>
                    <span style="font-size:0.85rem; color:#64748b; line-height:1.4; display:block;">
                        * Esta factura comercial representa el cobro del servicio prestado por el taller.<br/>
                        * Mecánico Asignado: <strong>${orden.nombreMecanico}</strong> (Doc. ${orden.docEmpleFk})
                    </span>
                </div>
                <div class="btn-group">
                    <a href="${pageContext.request.contextPath}/OrdenController?action=listAll" class="admin-btn admin-btn--danger back-btn" style="box-shadow:none;">Volver a Órdenes</a>
                    <button onclick="window.print();" class="admin-btn print-btn" style="box-shadow:none;">🖨️ Imprimir Factura</button>

                    <%-- Si ya está FACTURADA, mostrar badge informativo --%>
                    <c:if test="${orden.estado == 'FACTURADO'}">
                        <span style="display:inline-flex; align-items:center; gap:8px; padding:8px 16px; background:rgba(16,185,129,0.15); border:1px solid rgba(16,185,129,0.4); border-radius:10px; color:#10b981; font-weight:700; font-size:0.9rem;">
                            ✅ Pago Registrado
                            <c:if test="${not empty facturaRegistrada}">
                                — ${facturaRegistrada.metodoPago}
                            </c:if>
                        </span>
                    </c:if>

                    <%-- Si está TERMINADO, permitir registrar el pago --%>
                    <c:if test="${orden.estado == 'TERMINADO'}">
                        <!-- Formulario visible solo cuando la orden está TERMINADA, permite registrar el pago y cambiar el estado a FACTURADO mediante POST -->
                        <form action="${pageContext.request.contextPath}/OrdenController" method="post" style="display:inline-flex; align-items:center; gap:10px; vertical-align:middle;">
                            <input type="hidden" name="action" value="updateStatus" />
                            <input type="hidden" name="id_orden" value="${orden.idOrden}" />
                            <input type="hidden" name="nuevo_estado" value="FACTURADO" />
                            <select name="metodo_pago" class="form-input" style="font-size:0.9rem; width:auto; padding:8px 12px; margin:0; border:1px solid rgba(255,255,255,0.1); background:rgba(0,0,0,0.2); color:#fff; border-radius:8px; display:inline-block; vertical-align:middle;">
                                <option value="EFECTIVO" style="background:#1e293b;">💵 Efectivo</option>
                                <option value="TARJETA" style="background:#1e293b;">💳 Tarjeta</option>
                                <option value="TRANSFERENCIA" style="background:#1e293b;">📱 Transferencia</option>
                            </select>
                            <button type="submit" class="admin-btn payment-btn" style="background: linear-gradient(135deg, #10b981, #059669); box-shadow: none; margin:0; vertical-align:middle;">💵 Registrar Pago</button>
                        </form>
                    </c:if>
                </div>
            </footer>
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
