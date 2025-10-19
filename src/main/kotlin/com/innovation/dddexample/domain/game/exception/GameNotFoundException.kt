package com.innovation.dddexample.domain.game.exception

import com.innovation.dddexample.domain.common.exception.NotFoundException

class GameNotFoundException(id: Long) : NotFoundException("Game with id $id not found.")
