<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <!DOCTYPE html>
        <html lang="es">

        <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/variables.css" />
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css" />
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/navbar.css?v=1.1" />
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-panel.css" />

            <title>Gestión de Mecánicos | MotorDesk</title>
            <style>
                .admin-alert {
                    padding: 1rem;
                    margin-bottom: 1.5rem;
                    border-radius: 8px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    animation: slideIn 0.3s ease-out;
                }
                .admin-alert--success { background-color: rgba(46, 204, 113, 0.2); border: 1px solid #2ecc71; color: #2ecc71; }
                .admin-alert--error { background-color: rgba(231, 76, 60, 0.2); border: 1px solid #e74c3c; color: #e74c3c; }
                .admin-alert--info { background-color: rgba(52, 152, 219, 0.2); border: 1px solid #3498db; color: #3498db; }
                .admin-alert__close { background: none; border: none; font-size: 1.2rem; cursor: pointer; color: inherit; opacity: 0.7; }
                .admin-alert__close:hover { opacity: 1; }
                @keyframes slideIn { from { transform: translateY(-20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }

                /* Quitar flechitas del input de número (Documento) */
                input[type=number]::-webkit-inner-spin-button,
                input[type=number]::-webkit-outer-spin-button {
                    -webkit-appearance: none;
                    margin: 0;
                }
                input[type=number] {
                    -moz-appearance: textfield;
                }
            </style>
        </head>

        <body>
            <header class="navbar">
                <div class="navbar__logo">
                    <img src="${pageContext.request.contextPath}/LogoI_mg/Logo_blanco.png" alt="Logo MotorDesk"
                        class="navbar__logo-img" />
                </div>

                <nav class="navbar__menu" aria-label="Menu principal">
                    <a href="${pageContext.request.contextPath}/AdminDashboard" class="navbar__menu-item">Dashboard</a>
                    <a href="${pageContext.request.contextPath}/MecanicoController"
                        class="navbar__menu-item active">Mecánicos</a>
                    <a href="${pageContext.request.contextPath}/ClienteController" class="navbar__menu-item">Clientes</a>
                    <a href="${pageContext.request.contextPath}/ProductoController" class="navbar__menu-item">Productos</a>
                    <a href="${pageContext.request.contextPath}/admin/ingreso" class="navbar__menu-item">Ingresar Pedido</a>
                    <a href="${pageContext.request.contextPath}/admin/historialCompras" class="navbar__menu-item">Historial Compras</a>
                    <a href="${pageContext.request.contextPath}/OrdenController?action=listAll" class="navbar__menu-item">Órdenes</a>
                </nav>

                <div class="navbar__session">
                    <div class="navbar__user-info">
                        <span class="navbar__user-name">${sessionScope.usuarioLogueado.nombre}</span>

                    </div>
                    <a href="#logoutModal" class="navbar__session-btn">
                        <img src="${pageContext.request.contextPath}/LogoI_mg/cerrarseccion_blanco.png"
                            alt="Cerrar sesión" class="navbar__session-icon" />
                    </a>
                </div>
            </header>

            <main class="admin-main fade-in">
                <!-- Alertas de feedback -->
                <c:if test="${not empty sessionScope.mensaje}">
                    <div class="admin-alert admin-alert--${sessionScope.tipoMensaje}">
                        <span class="admin-alert__text">${sessionScope.mensaje}</span>
                        <button class="admin-alert__close" onclick="this.parentElement.remove();">×</button>
                    </div>
                    <% session.removeAttribute("mensaje"); session.removeAttribute("tipoMensaje"); %>
                </c:if>

                <section id="mecanicos" class="admin-section">
                    <header>
                        <h2 class="admin-section__title">Gestión de Mecánicos</h2>
                        <p class="admin-section__subtitle">Administra los usuarios pertenecientes al taller.</p>
                    </header>



                    <div class="admin-table-container">
                        <table class="admin-table">
                            <thead class="admin-table__head">
                                <tr>
                                    <th class="admin-table__th">Documento</th>
                                    <th class="admin-table__th">Nombres</th>
                                    <th class="admin-table__th">Cargo</th>
                                    <th class="admin-table__th">Fecha Ingreso</th>
                                    <th class="admin-table__th">Estado</th>
                                    <th class="admin-table__th">Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty requestScope.listaMecanicos}">
                                        <c:forEach var="mecanico" items="${requestScope.listaMecanicos}">
                                            <tr class="admin-table__row">
                                                <td class="admin-table__td">
                                                    <c:out value="${mecanico.idEmpleado}" />
                                                </td>
                                                <td class="admin-table__td">
                                                    <c:out value="${mecanico.nombre}" />
                                                </td>
                                                <td class="admin-table__td">
                                                    <c:choose>
                                                        <c:when test="${mecanico.idCargo == 1}">Administrador</c:when>
                                                        <c:when test="${mecanico.idCargo == 2}">Mecánico</c:when>
                                                        <c:otherwise>Desconocido</c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="admin-table__td">
                                                    <c:out value="${mecanico.fechaIngreso}" />
                                                </td>
                                                <td class="admin-table__td">
                                                    <c:choose>
                                                        <c:when test="${mecanico.estadoEmpleado == 'ACTIVO'}">
                                                            <span class="admin-badge admin-badge--active">Activo</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span
                                                                class="admin-badge admin-badge--inactive">Inactivo</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="admin-table__td">
                                                    <div class="admin-table__actions">
                                                        <a href="${pageContext.request.contextPath}/MecanicoController?action=edit&id=${mecanico.idEmpleado}#formMecanico"
                                                            class="admin-action-btn admin-action-btn--edit"
                                                            title="Editar">✏️ Editar</a>
                                                        <form
                                                            action="${pageContext.request.contextPath}/MecanicoController"
                                                            method="post" class="admin-action-form">
                                                            <input type="hidden" name="action" value="toggleState">
                                                            <input type="hidden" name="id" value="${mecanico.idEmpleado}">
                                                            <c:choose>
                                                                <c:when test="${mecanico.estadoEmpleado == 'ACTIVO'}">
                                                                    <button type="submit"
                                                                        class="admin-action-btn admin-action-btn--delete"
                                                                        title="Desactivar">🚫 Desactivar</button>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <button type="submit"
                                                                        class="admin-action-btn admin-action-btn--active"
                                                                        title="Activar">✅ Activar</button>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </form>
                                                        <a href="#deleteModal-${mecanico.idEmpleado}" class="admin-action-btn admin-action-btn--delete" title="Eliminar">🗑️ Eliminar</a>

                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr class="admin-table__row">
                                            <td class="admin-table__td" colspan="6" style="text-align: center;">No hay
                                                mecánicos registrados.</td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <article id="formMecanico" class="admin-form-section">
                        <h3 class="admin-form-section__title">${not empty requestScope.empleadoEditar ? 'Editar Mecánico' : 'Registrar Mecánico'}</h3>
                        <form class="admin-form" action="${pageContext.request.contextPath}/MecanicoController"
                            method="post">

                            <input type="hidden" name="action" value="${not empty requestScope.empleadoEditar ? 'update' : 'insert'}">

                            <div class="admin-form__group">
                                <label class="admin-form__label" for="doc_emple">Documento:</label>
                                <input class="admin-form__input" type="text" id="doc_emple" name="doc_emple" required
                                    placeholder="Ej: 123456789" value="${requestScope.empleadoEditar.idEmpleado}" ${not empty requestScope.empleadoEditar ? 'readonly' : ''}
                                    minlength="9" maxlength="10" pattern="\d{9,10}" title="El documento debe tener 9 o 10 números" oninput="this.value = this.value.replace(/[^0-9]/g, '')">
                            </div>

                            <div class="admin-form__group">
                                <label class="admin-form__label" for="nom_empleado">Nombre:</label>
                                <input class="admin-form__input" type="text" id="nom_empleado" name="nom_empleado"
                                    required placeholder="Nombre y Apellidos" value="${requestScope.empleadoEditar.nombre}" autocomplete="off"
                                    pattern="^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$" title="Solo se permiten letras y espacios" oninput="this.value = this.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\s]/g, '')">
                            </div>

                            <div class="admin-form__group">
                                <label class="admin-form__label" for="pin_acceso">PIN:</label>
                                <input class="admin-form__input" type="password" id="pin_acceso" name="pin_acceso"
                                    maxlength="10" required placeholder="Ingrese de 4 a 10 dígitos" value="${requestScope.empleadoEditar.pin}" autocomplete="new-password"
                                    pattern="\d{4,10}" title="El PIN debe contener entre 4 y 10 números enteros" oninput="this.value = this.value.replace(/[^0-9]/g, '')">
                            </div>

                            <!-- Ocultos porque el admin crea mecánicos -->
                            <input type="hidden" name="id_rol_fk" value="${not empty requestScope.empleadoEditar ? requestScope.empleadoEditar.idRol : '2'}">
                            <input type="hidden" name="id_cargo_fk" value="${not empty requestScope.empleadoEditar ? requestScope.empleadoEditar.idCargo : '2'}">

                            <div class="admin-form__actions">
                                <c:choose>
                                    <c:when test="${not empty requestScope.empleadoEditar}">
                                        <a href="${pageContext.request.contextPath}/MecanicoController" class="admin-btn admin-btn--danger" style="text-decoration: none; display: flex; align-items: center; justify-content: center;">Cancelar</a>
                                        <button type="submit" class="admin-btn">Actualizar Cambios</button>
                                    </c:when>
                                    <c:otherwise>
                                        <button type="reset" class="admin-btn admin-btn--danger">Limpiar</button>
                                        <button type="submit" class="admin-btn">Guardar Mecánico</button>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                        </form>
                    </article>
                </section>

                <!-- Delete Modals (Renderizados fuera de la tabla para evitar problemas de z-index) -->
                <c:if test="${not empty requestScope.listaMecanicos}">
                    <c:forEach var="mecanico" items="${requestScope.listaMecanicos}">
                        <div id="deleteModal-${mecanico.idEmpleado}" class="modal-css">
                            <div class="modal-content-css">
                                <h2>¿Eliminar Mecánico?</h2>
                                <p>Estás a punto de eliminar a <strong>${mecanico.nombre}</strong>.<br>Esta acción no se puede deshacer.</p>
                                <div class="modal-buttons-css">
                                    <a href="#" class="btn-modal-css btn-modal-css--cancel">Cancelar</a>
                                    <form action="${pageContext.request.contextPath}/MecanicoController" method="post" style="display:inline;">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${mecanico.idEmpleado}">
                                        <button type="submit" class="btn-modal-css btn-modal-css--confirm">Sí, Eliminar</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </c:if>
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