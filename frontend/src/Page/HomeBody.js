import React from 'react'
import Banner from '../FerComponent/Banner'
import NewMovie from '../FerComponent/NewMovie'
import ListBook from '../FerComponent/ListMovie'
import ListMovieByGenre from '../FerComponent/ListMovieByGenre'
import Footer from '../FerComponent/Footer'

function HomeBody() {
    return (
        <>
            <Banner />
            <NewMovie />
            <ListMovieByGenre genre={ "Hành động" } num={1}/>
            <ListMovieByGenre genre={ "Việt Nam" } num={2}/> 
            <ListBook />
            <Footer />
        </>
    )
}

export default HomeBody