<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <meta name="description" content="Panel de control del mecanico en MotorDesk" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/variables.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/navbar.css?v=1.1" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/mecanico-panel.css" />
    <title>Panel Mecanico | MotorDesk</title>
</head>
<body>
    <!-- ===== NAVBAR: solo logo + sesion (nav interno por tabs) ===== -->
    <header class="navbar navbar--compact">
        <div class="navbar__logo">
            <img src="${pageContext.request.contextPath}/LogoI_mg/Logo_blanco.png" alt="Logo MotorDesk" class="navbar__logo-img" />
            <span class="navbar__app-name">MotorDesk</span>
        </div>
        <div class="navbar__session">
            <div class="navbar__user-info">
                <span class="navbar__user-name">${sessionScope.usuarioLogueado.nombre}</span>
                <span class="navbar__user-role">Mecanico</span>
            </div>
            <a href="#logoutModal" class="navbar__session-btn">
                <img src="${pageContext.request.contextPath}/LogoI_mg/cerrarseccion_blanco.png" alt="Cerrar sesion" class="navbar__session-icon" />
            </a>
        </div>
    </header>

    <main class="admin-main fade-in">

        <!-- Feedback Alert -->
        <c:if test="${not empty sessionScope.mensaje}">
            <div class="admin-alert admin-alert--${sessionScope.tipoMensaje}" style="padding: 1rem; margin-bottom: 1.5rem; border-radius: 8px; display: flex; justify-content: space-between; align-items: center; ${sessionScope.tipoMensaje == 'success' ? 'background-color: rgba(46, 204, 113, 0.2); border: 1px solid #2ecc71; color: #2ecc71;' : 'background-color: rgba(231, 76, 60, 0.2); border: 1px solid #e74c3c; color: #e74c3c;'}">
                <span class="admin-alert__text">${sessionScope.mensaje}</span>
                <button class="admin-alert__close" style="background: none; border: none; font-size: 1.2rem; cursor: pointer; color: inherit; opacity: 0.7;" onclick="this.parentElement.remove();">x</button>
            </div>
            <% session.removeAttribute("mensaje"); session.removeAttribute("tipoMensaje"); %>
        </c:if>

        <!-- ===== SECTION: RESUMEN ===== -->
        <section id="resumen" class="mecanico-section" <c:if test="${not empty param.seccion and param.seccion ne 'resumen'}">style="display:none;"</c:if>>
            <header class="admin-section__header">
                <h1 class="admin-section__title">Bienvenido, ${sessionScope.usuarioLogueado.nombre}</h1>
                <p class="admin-section__subtitle">Aqui tienes el resumen de tu jornada en MotorDesk.</p>
            </header>

            <!-- Stat Cards -->
            <div class="stats-grid">
                <article class="stat-card">
                    <div class="stat-card__icon">&#128203;</div>
                    <div class="stat-card__content">
                        <div class="stat-card__title">Ordenes Abiertas</div>
                        <div class="stat-card__value">${not empty requestScope.ordenesAbiertas ? requestScope.ordenesAbiertas : '0'}</div>
                    </div>
                </article>
                <article class="stat-card">
                    <div class="stat-card__icon">&#128337;</div>
                    <div class="stat-card__content">
                        <div class="stat-card__title">Servicios Hoy</div>
                        <div class="stat-card__value">${not empty requestScope.ordenesHoy ? requestScope.ordenesHoy : '0'}</div>
                    </div>
                </article>
                <article class="stat-card stat-card--warning">
                    <div class="stat-card__icon">&#9888;</div>
                    <div class="stat-card__content">
                        <div class="stat-card__title">Alertas de Stock</div>
                        <div class="stat-card__value">${not empty requestScope.stockBajo ? requestScope.stockBajo.size() : '0'}</div>
                    </div>
                </article>
            </div>

            <!-- Quick Panels -->
            <div class="quick-grid">
                <!-- Recent Orders -->
                <div class="admin-card">
                    <div class="admin-card__header-row">
                        <h3 class="admin-card__title">Mis Ordenes Recientes</h3>
                        <a href="?seccion=ordenes" class="admin-btn admin-btn--small" style="text-decoration:none;">+ Nueva</a>
                    </div>
                    <div class="scroll-list">
                        <c:choose>
                            <c:when test="${not empty requestScope.listaOrdenes}">
                                <c:forEach var="ord" items="${requestScope.listaOrdenes}">
                                    <div class="order-mini-card">
                                        <div class="order-mini-card__top">
                                            <span class="order-mini-card__id">Orden #${ord.idOrden} (Placa: ${not empty ord.placaVehiculo ? ord.placaVehiculo : 'S/P'})</span>
                                            <span class="status-badge status-badge--${ord.estado.toLowerCase()}">${ord.estado}</span>
                                        </div>
                                        <p class="order-mini-card__desc">${ord.descripcion}</p>
                                        <div style="display:flex; justify-content:space-between; align-items:center; font-size:0.85em; opacity:0.8; margin-top:5px;">
                                            <span class="order-mini-card__date">
                                                <fmt:formatDate value="${ord.fecha}" pattern="dd/MM/yyyy" />
                                            </span>
                                            <span>Total: <strong><fmt:formatNumber value="${ord.total}" type="currency" currencySymbol="$" /></strong></span>
                                        </div>
                                        <c:if test="${ord.estado ne 'FACTURADO' and ord.estado ne 'TERMINADO'}">
                                            <div style="margin-top: 12px; border-top: 1px dashed rgba(255,255,255,0.1); padding-top: 10px; display: flex; gap: 8px; align-items: center; justify-content: space-between;">
                                                <form action="${pageContext.request.contextPath}/OrdenController" method="post" style="display: flex; gap: 6px; align-items: center; flex: 1;">
                                                    <input type="hidden" name="action" value="updateStatusMecanico" />
                                                    <input type="hidden" name="id_orden" value="${ord.idOrden}" />
                                                    <select name="nuevo_estado" class="form-input" style="padding: 4px 8px; font-size: 0.8rem; background: rgba(0,0,0,0.4); color: #fff; border: 1px solid rgba(255,255,255,0.1); border-radius: 6px; cursor: pointer; flex: 1; min-width: 110px;" required>
                                                        <option value="ABIERTA" ${ord.estado == 'ABIERTA' ? 'selected' : ''}>Abierta</option>
                                                        <option value="PROCESO" ${ord.estado == 'PROCESO' ? 'selected' : ''}>En Proceso</option>
                                                        <option value="ESPERA" ${ord.estado == 'ESPERA' ? 'selected' : ''}>Espera Repuestos</option>
                                                        <option value="TERMINADO" ${ord.estado == 'TERMINADO' ? 'selected' : ''}>Terminado</option>
                                                    </select>
                                                    <button type="submit" class="admin-btn admin-btn--small" style="padding: 4px 8px; font-size: 0.75rem; box-shadow: none; min-width: fit-content;">Actualizar</button>
                                                </form>
                                                <c:if test="${ord.estado == 'ABIERTA' or ord.estado == 'PROCESO'}">
                                                    <a href="${pageContext.request.contextPath}/PanelMecanicoController?seccion=ordenes&action=edit&id_orden=${ord.idOrden}" class="admin-btn admin-btn--small" style="text-decoration:none; padding: 4px 8px; font-size: 0.75rem; box-shadow: none; border: 1px solid rgba(59,130,246,0.3); background: rgba(59,130,246,0.1); color: #3b82f6; min-width: fit-content;">Editar</a>
                                                </c:if>
                                            </div>
                                        </c:if>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-state">
                                    <span class="empty-state__icon">&#128203;</span>
                                    <p>No tienes ordenes abiertas.</p>
                                    <a href="?seccion=ordenes" class="admin-btn admin-btn--small" style="text-decoration:none;">Crear primera orden</a>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- Stock Alerts -->
                <div class="admin-card">
                    <h3 class="admin-card__title">Alertas de Inventario</h3>
                    <div class="scroll-list">
                        <c:choose>
                            <c:when test="${not empty requestScope.stockBajo}">
                                <c:forEach var="p" items="${requestScope.stockBajo}">
                                    <div class="stock-alert-item">
                                        <span class="stock-alert-item__name">${p.nombreProducto}</span>
                                        <span class="stock-alert-item__qty">${p.stock} uds.</span>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div class="empty-state">
                                    <span class="empty-state__icon">&#10003;</span>
                                    <p>Inventario en buen estado.</p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </section>

        <!-- ===== SECTION: NUEVA / EDITAR ORDEN ===== -->
        <section id="ordenes" class="mecanico-section" <c:if test="${param.seccion ne 'ordenes'}">style="display:none;"</c:if>>
            <header class="admin-section__header">
                <c:choose>
                    <c:when test="${not empty requestScope.ordenEditar}">
                        <h1 class="admin-section__title">Editar Orden de Trabajo #${requestScope.ordenEditar.idOrden}</h1>
                        <p class="admin-section__subtitle">Modifica la información de la orden activa.</p>
                    </c:when>
                    <c:otherwise>
                        <h1 class="admin-section__title">Nueva Orden de Trabajo</h1>
                        <p class="admin-section__subtitle">Registra un nuevo servicio del taller.</p>
                    </c:otherwise>
                </c:choose>
            </header>

            <div class="admin-card" style="max-width: 750px;">
                <form action="${pageContext.request.contextPath}/OrdenController" method="post" class="admin-form" id="orderForm">
                    <input type="hidden" name="action" value="${not empty requestScope.ordenEditar ? 'update' : 'insert'}" />
                    <c:if test="${not empty requestScope.ordenEditar}">
                        <input type="hidden" name="id_orden" value="${requestScope.ordenEditar.idOrden}" />
                    </c:if>
                    <input type="hidden" name="id_mecanico" value="${sessionScope.usuarioLogueado.idEmpleado}" />

                    <!-- DATOS DEL DUEÑO (CLIENTE) -->
                    <h3 style="margin-top: 1rem; margin-bottom: 0.5rem; color: #fff; font-size: 1.1rem; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 0.5rem;">1. Datos del Dueño (Cliente)</h3>
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                        <div class="form-group">
                            <label class="form-label">Documento (Cédula)</label>
                            <input type="text" name="doc_cliente" class="form-input" placeholder="Ej: 123456789" required
                                pattern="\d+" minlength="6" maxlength="15" title="Solo números" oninput="this.value = this.value.replace(/[^0-9]/g, '')" />
                        </div>
                        <div class="form-group">
                            <label class="form-label">Nombre Completo</label>
                            <input type="text" name="nom_cliente" class="form-input" placeholder="Nombres y Apellidos" required 
                                pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$" title="Solo letras y espacios" oninput="this.value = this.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\s]/g, '')" />
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Dirección</label>
                        <input type="text" name="direccion_cliente" class="form-input" placeholder="Ej: Calle 123 #45-67" required />
                    </div>

                    <!-- DATOS DEL VEHÍCULO -->
                    <h3 style="margin-top: 1.5rem; margin-bottom: 0.5rem; color: #fff; font-size: 1.1rem; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 0.5rem;">2. Datos del Vehículo</h3>
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                        <div class="form-group">
                            <label class="form-label">Placa del Vehículo</label>
                            <input type="text" name="placa" class="form-input" placeholder="Ej: ABC-123" value="${not empty requestScope.ordenEditar ? requestScope.ordenEditar.placaVehiculo : ''}" required 
                                maxlength="7" oninput="this.value = this.value.toUpperCase()" />
                        </div>
                        <div class="form-group">
                            <label class="form-label">Marca</label>
                            <input type="text" name="marca" class="form-input" placeholder="Ej: Chevrolet" required />
                        </div>
                        <div class="form-group">
                            <label class="form-label">Modelo</label>
                            <input type="text" name="modelo" class="form-input" placeholder="Ej: Spark" required />
                        </div>
                        <div class="form-group">
                            <label class="form-label">Año</label>
                            <input type="number" name="anio" class="form-input" placeholder="Ej: 2018" required min="1900" max="<%= java.time.Year.now().getValue() %>" title="El modelo no puede ser mayor al año actual" />
                        </div>
                    </div>

                    <!-- DATOS DEL SERVICIO -->
                    <h3 style="margin-top: 1.5rem; margin-bottom: 0.5rem; color: #fff; font-size: 1.1rem; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 0.5rem;">3. Servicios de Mano de Obra</h3>
                    <p style="font-size: 0.82rem; color: #94a3b8; margin-bottom: 0.8rem;">Agrega uno o más servicios que se realizarán en esta orden. Ej: Despinche, Revisión suspensión, Cambio de aceite.</p>

                    <div id="servicios-container" style="display: flex; flex-direction: column; gap: 10px; margin-bottom: 10px;">

                        <%-- Pre-llenar servicios al editar --%>
                        <c:choose>
                            <c:when test="${not empty requestScope.serviciosEditar}">
                                <c:forEach var="srv" items="${requestScope.serviciosEditar}" varStatus="loop">
                                <div class="servicio-row" style="display:grid; grid-template-columns:2fr 1fr auto; gap:10px; align-items:center;">
                                    <input type="text" name="servicios[]" class="form-input"
                                           placeholder="Ej: Despinche llanta trasera"
                                           value="${srv.nombre}"
                                           ${loop.index == 0 ? 'required' : ''}
                                           style="font-size:0.9rem;" />
                                    <input type="number" name="valoresServicio[]" class="form-input"
                                           placeholder="Valor $" min="0" step="100"
                                           value="${srv.valor}"
                                           style="font-size:0.9rem;" />
                                    <c:if test="${loop.index > 0}">
                                        <button type="button" onclick="eliminarServicio(this)"
                                                style="background:rgba(239,68,68,0.15);border:1px solid rgba(239,68,68,0.3);color:#ef4444;border-radius:8px;padding:6px 10px;cursor:pointer;font-size:1rem;line-height:1;">🗑</button>
                                    </c:if>
                                    <c:if test="${loop.index == 0}">
                                        <span style="width:34px;"></span>
                                    </c:if>
                                </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <%-- Fila inicial vacía para nuevas órdenes --%>
                                <div class="servicio-row" style="display:grid; grid-template-columns:2fr 1fr auto; gap:10px; align-items:center;">
                                    <input type="text" name="servicios[]" class="form-input"
                                           placeholder="Ej: Despinche llanta trasera" required
                                           style="font-size:0.9rem;" />
                                    <input type="number" name="valoresServicio[]" class="form-input"
                                           placeholder="Valor $" min="0" step="100"
                                           style="font-size:0.9rem;" />
                                    <span style="width:34px;"></span>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <button type="button" onclick="agregarServicio()"
                            style="background:rgba(59,130,246,0.1);border:1px solid rgba(59,130,246,0.3);color:#3b82f6;
                                   border-radius:8px;padding:7px 14px;cursor:pointer;font-size:0.85rem;
                                   margin-bottom:1.2rem;transition:all .2s;">
                        ➕ Agregar otro servicio
                    </button>

                    <%-- Campo oculto: resumen de descripción generado por JS al enviar el form --%>
                    <input type="hidden" name="descripcion" id="descripcionResumen" />


                    <div class="form-group">
                        <label class="form-label">Productos Utilizados</label>
                        <div id="repu-container" style="display: flex; flex-direction: column; gap: 10px; margin-bottom: 10px;">
                            <c:forEach begin="0" end="4" var="i">
                            <c:set var="det" value="${requestScope.detallesEditar[i]}" />
                            <div class="repu-row" style="display:grid; grid-template-columns:2fr 1fr; gap:10px; align-items:center;">
                                <select name="productos[]" class="form-input">
                                    <option value="">-- Seleccionar Repuesto --</option>
                                    <c:forEach var="p" items="${requestScope.listaProductos}">
                                        <option value="${p.idProducto}" ${not empty det and det.idProductoFk == p.idProducto ? 'selected' : ''}>${p.nombreProducto} - $<fmt:formatNumber value="${p.precioUnitario}" pattern="###,##0.00"/></option>
                                    </c:forEach>
                                </select>
                                <input type="number" name="cantidades[]" class="form-input" min="1" placeholder="Cant." value="${not empty det ? det.cantidad : ''}" />
                                <input type="hidden" name="precios[]" value="0" />
                            </div>
                            </c:forEach>
                        </div>
                    </div>

                    <div style="display:flex; gap: 1rem; margin-top: 0.5rem;">
                        <button type="submit" class="admin-btn">${not empty requestScope.ordenEditar ? 'Guardar Cambios' : 'Abrir Orden de Trabajo'}</button>
                        <a href="?seccion=resumen" class="admin-btn admin-btn--danger" style="text-decoration:none; text-align:center;">Cancelar</a>
                    </div>
                </form>
            </div>
        </section>

        <!-- ===== SECTION: INVENTARIO ===== -->
        <section id="inventario" class="mecanico-section" <c:if test="${param.seccion ne 'inventario'}">style="display:none;"</c:if>>
            <header class="admin-section__header">
                <h1 class="admin-section__title">Inventario de Productos</h1>
                <p class="admin-section__subtitle">Consulta la disponibilidad actual de productos.</p>
            </header>

            <div class="admin-card">
                <c:choose>
                    <c:when test="${not empty requestScope.listaProductos}">
                        <div class="product-grid">
                            <c:forEach var="p" items="${requestScope.listaProductos}">
                                <div class="product-card ${p.stock < 5 ? 'product-card--low' : ''}">
                                    <div class="product-card__icon">&#128230;</div>
                                    <div class="product-card__info">
                                        <span class="product-card__name">${p.nombreProducto}</span>
                                        <span class="product-card__price">$<fmt:formatNumber value="${p.precioUnitario}" pattern="###,##0.00"/></span>
                                    </div>
                                    <div class="product-card__stock ${p.stock < 5 ? 'product-card__stock--low' : ''}">
                                        ${p.stock} uds
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state">
                            <span class="empty-state__icon">&#128230;</span>
                            <p>No hay productos en inventario.</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
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

    <script>
        // ===== SERVICIOS DINÁMICOS =====

        // Agrega una nueva fila de servicio al contenedor
        function agregarServicio() {
            var container = document.getElementById('servicios-container');
            var fila = document.createElement('div');
            fila.className = 'servicio-row';
            fila.style.cssText = 'display:grid; grid-template-columns:2fr 1fr auto; gap:10px; align-items:center;';
            fila.innerHTML =
                '<input type="text" name="servicios[]" class="form-input" ' +
                '       placeholder="Ej: Revisión de suspensión" style="font-size:0.9rem;" />' +
                '<input type="number" name="valoresServicio[]" class="form-input" ' +
                '       placeholder="Valor $" min="0" step="100" style="font-size:0.9rem;" />' +
                '<button type="button" onclick="eliminarServicio(this)" ' +
                '        style="background:rgba(239,68,68,0.15);border:1px solid rgba(239,68,68,0.3);' +
                '               color:#ef4444;border-radius:8px;padding:6px 10px;cursor:pointer;' +
                '               font-size:1rem;line-height:1;">🗑</button>';
            container.appendChild(fila);
            // Foco en el nuevo campo de nombre
            fila.querySelector('input[name="servicios[]"]').focus();
        }

        // Elimina la fila de servicio correspondiente
        function eliminarServicio(btn) {
            var fila = btn.closest('.servicio-row');
            if (fila) fila.remove();
        }

        // Al enviar el formulario, genera automáticamente el campo descripcion
        // como resumen legible de todos los servicios (para mantener compatibilidad con el campo descripcion de BD)
        var orderForm = document.getElementById('orderForm');
        if (orderForm) {
            orderForm.addEventListener('submit', function() {
                var inputs = document.querySelectorAll('#servicios-container input[name="servicios[]"]');
                var nombres = [];
                inputs.forEach(function(inp) {
                    if (inp.value.trim()) nombres.push(inp.value.trim());
                });
                var resumen = nombres.length > 0 ? nombres.join(' | ') : 'Servicio general';
                document.getElementById('descripcionResumen').value = resumen;
            });
        }
    </script>
</body>
</html>