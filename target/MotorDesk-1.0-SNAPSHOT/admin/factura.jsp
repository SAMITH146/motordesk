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
    <title>Factura Comercial | MotorDesk</title>
    <style>
        .factura-card {
            background: linear-gradient(145deg, #1e293b, #0f172a);
            border: 1px solid rgba(59, 130, 246, 0.2);
            border-radius: 20px;
            padding: 2.5rem;
            box-shadow: 0 12px 40px rgba(0,0,0,0.4);
            margin-top: 1rem;
            color: #f8fafc;
        }

        .factura-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            border-bottom: 2px solid rgba(255, 255, 255, 0.08);
            padding-bottom: 2rem;
            margin-bottom: 2rem;
        }

        .factura-header__taller {
            display: flex;
            flex-direction: column;
            gap: 5px;
        }

        .factura-header__logo {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 10px;
        }

        .factura-header__logo-img {
            height: 45px;
            width: auto;
        }

        .factura-header__logo-text {
            font-size: 1.8rem;
            font-weight: 800;
            color: #fff;
            letter-spacing: 1px;
        }

        .factura-header__info {
            font-size: 0.9rem;
            color: #94a3b8;
            line-height: 1.5;
        }

        .factura-header__meta {
            text-align: right;
            display: flex;
            flex-direction: column;
            gap: 8px;
        }

        .factura-header__meta-title {
            font-size: 1.6rem;
            font-weight: 800;
            color: #3b82f6;
            letter-spacing: 1px;
        }

        .factura-header__meta-id {
            font-size: 1.2rem;
            font-weight: 700;
            color: #fff;
        }

        .factura-header__meta-text {
            font-size: 0.9rem;
            color: #94a3b8;
        }

        .factura-status {
            display: inline-block;
            padding: 6px 14px;
            border-radius: 50px;
            font-size: 0.8rem;
            font-weight: 800;
            text-transform: uppercase;
            letter-spacing: 1px;
            margin-top: 5px;
        }

        .factura-status--abierta { background: rgba(59, 130, 246, 0.15); border: 1px solid rgba(59, 130, 246, 0.3); color: #3b82f6; }
        .factura-status--proceso { background: rgba(245, 158, 11, 0.15); border: 1px solid rgba(245, 158, 11, 0.3); color: #f59e0b; }
        .factura-status--espera { background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.3); color: #ef4444; }
        .factura-status--terminado { background: rgba(16, 185, 129, 0.15); border: 1px solid rgba(16, 185, 129, 0.3); color: #10b981; }
        .factura-status--facturado { background: rgba(168, 85, 247, 0.15); border: 1px solid rgba(168, 85, 247, 0.3); color: #a855f7; }

        .factura-details-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 2.5rem;
            margin-bottom: 2.5rem;
        }

        .factura-section-block {
            background: rgba(15, 23, 42, 0.4);
            border: 1px solid rgba(255, 255, 255, 0.05);
            border-radius: 15px;
            padding: 1.5rem;
        }

        .factura-section-block__title {
            font-size: 1.05rem;
            font-weight: 800;
            color: #fff;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 1rem;
            border-bottom: 1px solid rgba(255, 255, 255, 0.08);
            padding-bottom: 0.5rem;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .factura-section-block__item {
            font-size: 0.95rem;
            line-height: 1.6;
            margin-bottom: 0.5rem;
            display: flex;
            justify-content: space-between;
        }

        .factura-section-block__label {
            color: #94a3b8;
            font-weight: 600;
        }

        .factura-section-block__val {
            color: #f1f5f9;
            font-weight: 700;
            text-align: right;
        }

        .factura-items-table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 2rem;
        }

        .factura-items-table th {
            background: rgba(30, 41, 59, 0.6);
            border-bottom: 2px solid rgba(255, 255, 255, 0.08);
            padding: 12px 15px;
            font-size: 0.85rem;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #94a3b8;
            font-weight: 700;
            text-align: left;
        }

        .factura-items-table td {
            padding: 12px 15px;
            border-bottom: 1px solid rgba(255, 255, 255, 0.05);
            font-size: 0.95rem;
            color: #f1f5f9;
        }

        .factura-items-table tr:hover td {
            background: rgba(59, 130, 246, 0.03);
        }

        .factura-totals-container {
            display: flex;
            justify-content: flex-end;
            margin-top: 1.5rem;
        }

        .factura-totals-box {
            width: 320px;
            background: rgba(30, 41, 59, 0.4);
            border: 1px solid rgba(255, 255, 255, 0.05);
            border-radius: 12px;
            padding: 1.2rem;
        }

        .factura-total-row {
            display: flex;
            justify-content: space-between;
            padding: 8px 0;
            font-size: 0.95rem;
        }

        .factura-total-row--grand {
            border-top: 1px solid rgba(255, 255, 255, 0.1);
            margin-top: 8px;
            padding-top: 12px;
            font-size: 1.25rem;
            font-weight: 800;
            color: #3b82f6;
        }

        .factura-actions {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 2.5rem;
            border-top: 1px solid rgba(255, 255, 255, 0.08);
            padding-top: 2rem;
            gap: 15px;
        }

        .btn-group {
            display: flex;
            gap: 10px;
        }

        .factura-watermark {
            display: none;
        }

        @media (max-width: 768px) {
            .factura-details-grid {
                grid-template-columns: 1fr;
                gap: 1.5rem;
            }
            .factura-card {
                padding: 1.5rem;
            }
            .factura-header {
                flex-direction: column;
                gap: 1.5rem;
            }
            .factura-header__meta {
                text-align: left;
            }
            .factura-totals-box {
                width: 100%;
            }
            .factura-actions {
                flex-direction: column;
                gap: 1.5rem;
                align-items: stretch;
            }
            .btn-group {
                flex-direction: column;
            }
        }

        /* ===== IMPRESIÓN LIMPIA ===== */
        @media print {
            body {
                background: #fff !important;
                color: #000 !important;
                font-family: Arial, sans-serif !important;
            }
            
            .navbar, 
            .navbar__menu, 
            .navbar__session, 
            .navbar__logo, 
            .factura-actions,
            #logoutModal {
                display: none !important;
            }
            
            .admin-main {
                padding: 0 !important;
                margin: 0 !important;
                max-width: 100% !important;
            }
            
            .factura-card {
                background: #fff !important;
                border: none !important;
                padding: 0 !important;
                box-shadow: none !important;
                color: #000 !important;
                margin: 0 !important;
            }

            .factura-header {
                border-bottom: 2px solid #000 !important;
                padding-bottom: 1.5rem !important;
                margin-bottom: 1.5rem !important;
            }

            .factura-header__logo-text {
                color: #000 !important;
            }

            .factura-header__info {
                color: #333 !important;
            }

            .factura-header__meta-title {
                color: #000 !important;
            }

            .factura-header__meta-id {
                color: #000 !important;
            }

            .factura-header__meta-text {
                color: #333 !important;
            }

            .factura-status {
                border: 1px solid #000 !important;
                color: #000 !important;
                background: none !important;
            }

            .factura-section-block {
                background: #fff !important;
                border: 1px solid #ccc !important;
                color: #000 !important;
            }

            .factura-section-block__title {
                color: #000 !important;
                border-bottom: 1px solid #000 !important;
            }

            .factura-section-block__label {
                color: #333 !important;
            }

            .factura-section-block__val {
                color: #000 !important;
            }

            .factura-items-table th {
                background: #f1f5f9 !important;
                color: #000 !important;
                border-bottom: 2px solid #000 !important;
            }

            .factura-items-table td {
                color: #000 !important;
                border-bottom: 1px solid #e2e8f0 !important;
            }

            .factura-totals-box {
                background: #fff !important;
                border: 1px solid #ccc !important;
                color: #000 !important;
            }

            .factura-total-row--grand {
                border-top: 1px solid #000 !important;
                color: #000 !important;
            }

            .factura-watermark {
                display: block;
                position: absolute;
                top: 40%;
                left: 20%;
                transform: rotate(-30deg);
                font-size: 6rem;
                font-weight: 900;
                opacity: 0.08;
                color: #000;
                pointer-events: none;
                text-transform: uppercase;
            }
        }
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
            <a href="${pageContext.request.contextPath}/OrdenController?action=listAll" class="navbar__menu-item active">Órdenes</a>
        </nav>
        <div class="navbar__session">
            <span class="navbar__user-name">${sessionScope.usuarioLogueado.nombre}</span>
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
                        <em>Bogotá D.C., Colombia</em>
                    </div>
                </div>

                <div class="factura-header__meta">
                    <span class="factura-header__meta-title">FACTURA COMERCIAL</span>
                    <span class="factura-header__meta-id">N° MD-<fmt:formatNumber value="${orden.idOrden}" pattern="0000"/></span>
                    <span class="factura-header__meta-text">
                        <strong>Fecha Emisión:</strong> <fmt:formatDate value="${orden.fecha}" pattern="dd/MM/yyyy" />
                    </span>
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

            <!-- DETALLE DE ITEMS -->
            <div class="factura-section-block" style="padding: 1rem 0 0 0; overflow-x: auto;">
                <h3 class="factura-section-block__title" style="padding: 0 1.5rem 0.5rem 1.5rem; margin-bottom: 0;">🔧 Detalle de Servicios y Repuestos</h3>
                <table class="factura-items-table">
                    <thead>
                        <tr>
                            <th>Cód. Producto</th>
                            <th>Descripción / Repuesto</th>
                            <th style="text-align: center;">Cantidad</th>
                            <th style="text-align: right;">Precio Unitario</th>
                            <th style="text-align: right;">Subtotal</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty requestScope.detalles}">
                                <c:forEach var="d" items="${requestScope.detalles}">
                                    <tr>
                                        <td>#${d.idProductoFk}</td>
                                        <td>${d.nombreProducto}</td>
                                        <td style="text-align: center;">${d.cantidad}</td>
                                        <td style="text-align: right;"><fmt:formatNumber value="${d.subtotal / d.cantidad}" type="currency" currencySymbol="$" /></td>
                                        <td style="text-align: right; font-weight: 700;"><fmt:formatNumber value="${d.subtotal}" type="currency" currencySymbol="$" /></td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="5" style="text-align: center; opacity: 0.5; padding: 2rem;">No se registraron repuestos en esta orden de servicio.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <!-- TOTALS -->
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
                    
                    <c:if test="${orden.estado == 'TERMINADO'}">
                        <form action="${pageContext.request.contextPath}/OrdenController" method="post" style="display:inline;">
                            <input type="hidden" name="action" value="updateStatus" />
                            <input type="hidden" name="id_orden" value="${orden.idOrden}" />
                            <input type="hidden" name="nuevo_estado" value="FACTURADO" />
                            <button type="submit" class="admin-btn payment-btn" style="background: linear-gradient(135deg, #10b981, #059669); box-shadow: none;">💵 Registrar Pago y Facturar</button>
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
