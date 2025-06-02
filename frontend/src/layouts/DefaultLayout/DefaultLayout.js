import React from 'react'
import Header from './Header'
import HomePage from '../../Page/Home/HomePage'

function DefaultLayout({ children }) {
  return (
    <div className='bg-dark'>
      <Header />
      <HomePage/>
      <h1>Hello</h1>
      <h1>Hello</h1>
      <h1>Hello</h1>

      <h1>Hello</h1>
      <h1>Hello</h1>
    </div>
  )
}

export default DefaultLayout