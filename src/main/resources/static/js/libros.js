document.addEventListener('DOMContentLoaded', function() {
    const searchButton = document.getElementById('searchButton');
    const titleInput = document.getElementById('title');
    const authorInput = document.getElementById('author');
    const subjectInput = document.getElementById('subject');
    const bookGrid = document.getElementById('bookGrid');
    const resultsSection = document.getElementById('resultsSection');
    const noResults = document.getElementById('noResults');
    const loading = document.getElementById('loading');
    const pagination = document.getElementById('pagination');
    const toast = document.getElementById('toast');
    
    let currentPage = 1;
    const itemsPerPage = 10;
    let totalResults = 0;
    let allBooks = [];
    let favoritesCache = {};
    
    searchButton.addEventListener('click', function() {
        performSearch(1);
    });
    
    // Permitir la búsqueda al presionar Enter en los campos de texto
    [titleInput, authorInput].forEach(input => {
        input.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                performSearch(1);
            }
        });
    });
    
    function performSearch(page) {
        const title = titleInput.value.trim();
        const author = authorInput.value.trim();
        const subject = subjectInput.value;
        
        if (title === '' && author === '' && subject === '') {
            alert('Por favor, ingresa al menos un criterio de búsqueda');
            return;
        }
        
        // Mostrar loader
        loading.style.display = 'block';
        resultsSection.style.display = 'none';
        noResults.style.display = 'none';
        
        // Construir la URL de búsqueda
        let searchQuery = [];
        
        if (title) {
            searchQuery.push(`title=${encodeURIComponent(title)}`);
        }
        
        if (author) {
            searchQuery.push(`author=${encodeURIComponent(author)}`);
        }
        
        if (subject) {
            searchQuery.push(`subject=${encodeURIComponent(subject)}`);
        }
        
        // Si hay título, autor o categoría específicos, usamos los parámetros correspondientes
        // Si no, usamos un término de búsqueda general
        let apiUrl;
        if (searchQuery.length > 0) {
            apiUrl = `https://openlibrary.org/search.json?${searchQuery.join('&')}&page=${page}`;
        } else {
            // Búsqueda general si no hay criterios específicos
            apiUrl = `https://openlibrary.org/search.json?q=${encodeURIComponent(title || author || '')}`;
        }
        
        fetch(apiUrl)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error en la respuesta de la API');
                }
                return response.json();
            })
            .then(data => {
                // Ocultar loader
                loading.style.display = 'none';
                
                if (data.docs && data.docs.length > 0) {
                    // Guardar todos los libros y la página actual
                    allBooks = data.docs;
                    currentPage = page;
                    totalResults = data.numFound;
                    
                    // Obtener el estado de favoritos para estos libros
                    checkFavoritesStatus(allBooks);
                    
                    // Mostrar sección de resultados
                    resultsSection.style.display = 'block';
                } else {
                    // Mostrar mensaje de no resultados
                    noResults.style.display = 'block';
                }
            })
            .catch(error => {
                console.error('Error:', error);
                loading.style.display = 'none';
                alert('Ocurrió un error al buscar los libros. Por favor, intenta de nuevo.');
            });
    }
    
    function checkFavoritesStatus(books) {
        // Crear un array de promesas para verificar todos los libros
        const keyPromises = books.map(book => {
            const key = book.key;
            
            // Si ya tenemos el estado en caché, no necesitamos consultarlo de nuevo
            if (favoritesCache[key] !== undefined) {
                return Promise.resolve();
            }
            
            return fetch(`/api/libros/favoritos/verificar/${key}`)
                .then(response => response.json())
                .then(data => {
                    favoritesCache[key] = data.esFavorito;
                })
                .catch(error => {
                    console.error('Error al verificar favorito:', error);
                    favoritesCache[key] = false;
                });
        });
        
        // Cuando todas las verificaciones terminen, mostrar los libros
        Promise.all(keyPromises).then(() => {
            displayBooks(books);
            updatePagination();
        });
    }
    
    function displayBooks(books) {
        // Limpiar el contenedor de libros
        bookGrid.innerHTML = '';
        
        // Mostrar los libros
        books.forEach(book => {
            // Crear elemento de tarjeta para el libro
            const bookCard = document.createElement('div');
            bookCard.className = 'book-card';
            
            // Obtener la imagen de la portada si está disponible
            let coverUrl = '/api/placeholder/250/300';
            if (book.cover_i) {
                coverUrl = `https://covers.openlibrary.org/b/id/${book.cover_i}-M.jpg`;
            }
            
            // Obtener el año de publicación si está disponible
            let publishYear = book.first_publish_year || (book.publish_year ? book.publish_year[0] : 'Desconocido');
            
            // Obtener autores si están disponibles
            let authors = book.author_name ? book.author_name.join(', ') : 'Autor desconocido';
            
            // Obtener descripción o usar un placeholder
            let description = 'No hay descripción disponible para este libro.';
            
            // Verificar si el libro ya está en favoritos
            const isFavorite = favoritesCache[book.key] || false;
            
            // Configurar el botón de favoritos según el estado
            const favoriteButtonClass = isFavorite ? 'favorite-button added' : 'favorite-button';
            const favoriteButtonText = isFavorite ? 'En favoritos' : 'Agregar a favoritos';
            
            // Estructura HTML de la tarjeta
            bookCard.innerHTML = `
                <div class="book-cover">
                    <img src="${coverUrl}" alt="Portada de ${book.title}" onerror="this.src='/api/placeholder/250/300'">
                </div>
                <div class="book-info">
                    <h3 class="book-title">${book.title}</h3>
                    <p class="book-author">${authors}</p>
                    <p class="book-year">${publishYear}</p>
                    <p class="book-description">${description}</p>
                    <div class="book-actions">
                        <button class="book-button details-button" data-key="${book.key}">Ver detalles</button>
                        <button class="book-button ${favoriteButtonClass}" 
                                data-key="${book.key}" 
                                data-title="${book.title}" 
                                data-author="${authors}" 
                                data-cover="${coverUrl}"
                                onclick="toggleFavorite(this)">
                            ${favoriteButtonText}
                        </button>
                    </div>
                </div>
            `;
            
            // Agregar evento para el botón de detalles
            bookCard.querySelector('.details-button').addEventListener('click', function() {
                const key = this.getAttribute('data-key');
                window.open(`https://openlibrary.org${key}`, '_blank');
            });
            
            // Agregar la tarjeta al grid
            bookGrid.appendChild(bookCard);
        });
    }
    
    function updatePagination() {
        // Limpiar paginación
        pagination.innerHTML = '';
        
        // Calcular número total de páginas
        const totalPages = Math.ceil(totalResults / itemsPerPage);
        
        // Si hay muchas páginas, mostrar solo un subconjunto
        let startPage = Math.max(1, currentPage - 2);
        let endPage = Math.min(totalPages, currentPage + 2);
        
        // Asegurar que siempre se muestren al menos 5 páginas si están disponibles
        if (endPage - startPage < 4) {
            if (startPage === 1) {
                endPage = Math.min(5, totalPages);
            } else {
                startPage = Math.max(1, endPage - 4);
            }
        }
        
        // Botón para primera página
        if (startPage > 1) {
            addPaginationButton(1, '«');
        }
        
        // Botones de páginas
        for (let i = startPage; i <= endPage; i++) {
            addPaginationButton(i, i.toString(), i === currentPage);
        }
        
        // Botón para última página
        if (endPage < totalPages) {
            addPaginationButton(totalPages, '»');
        }
    }
    
    function addPaginationButton(page, text, isActive = false) {
        const button = document.createElement('button');
        button.className = 'pagination-button';
        if (isActive) {
            button.classList.add('active');
        }
        button.textContent = text;
        
        button.addEventListener('click', function() {
            if (page !== currentPage) {
                performSearch(page);
            }
        });
        
        pagination.appendChild(button);
    }
});

// Función para mostrar mensajes en forma de toast
function showToast(message, type = 'normal') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = 'toast';
    if (type === 'success') {
        toast.classList.add('success');
    } else if (type === 'error') {
        toast.classList.add('error');
    }
    
    toast.classList.add('show');
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// Función para alternar el estado de favorito
function toggleFavorite(button) {
    const libroId = button.getAttribute('data-key');
    const titulo = button.getAttribute('data-title');
    const autor = button.getAttribute('data-author');
    const imagenUrl = button.getAttribute('data-cover');
    
    if (button.classList.contains('added')) {
        // Ya está en favoritos, hay que quitarlo
        fetch(`/api/libros/favoritos/eliminar/${libroId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => response.json())
        .then(data => {
            if (data.mensaje) {
                button.classList.remove('added');
                button.textContent = 'Agregar a favoritos';
                showToast('Libro eliminado de favoritos', 'success');
            } else if (data.error) {
                showToast(data.error, 'error');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('Error al eliminar de favoritos', 'error');
        });
    } else {
        // No está en favoritos, hay que agregarlo
        const formData = new FormData();
        formData.append('libroId', libroId);
        formData.append('titulo', titulo);
        formData.append('autor', autor);
        formData.append('imagenUrl', imagenUrl);
        
        fetch('/api/libros/favoritos/agregar', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            if (data.favorito) {
                button.classList.add('added');
                button.textContent = 'En favoritos';
                showToast('Libro agregado a favoritos', 'success');
            } else if (data.error) {
                showToast(data.error, 'error');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('Error al agregar a favoritos', 'error');
        });
    }
}