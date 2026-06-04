import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const items = ref(JSON.parse(localStorage.getItem('cart') || '[]'))

  function saveToLocal() {
    localStorage.setItem('cart', JSON.stringify(items.value))
  }

  const totalCount = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))
  const totalAmount = computed(() => items.value.reduce((sum, item) => sum + item.price * item.quantity, 0))

  function addItem(book) {
    const exist = items.value.find(i => i.bookId === book.id)
    if (exist) {
      exist.quantity++
    } else {
      items.value.push({
        bookId: book.id,
        title: book.title,
        coverImage: book.coverImage,
        price: book.price,
        quantity: 1,
      })
    }
    saveToLocal()
  }

  function removeItem(bookId) {
    items.value = items.value.filter(i => i.bookId !== bookId)
    saveToLocal()
  }

  function updateQuantity(bookId, quantity) {
    const item = items.value.find(i => i.bookId === bookId)
    if (item) {
      item.quantity = Math.max(1, quantity)
      saveToLocal()
    }
  }

  function clearCart() {
    items.value = []
    saveToLocal()
  }

  return { items, totalCount, totalAmount, addItem, removeItem, updateQuantity, clearCart }
})
